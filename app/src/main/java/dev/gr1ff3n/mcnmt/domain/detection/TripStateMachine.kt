package dev.gr1ff3n.mcnmt.domain.detection

import dev.gr1ff3n.mcnmt.domain.model.TripEndSource
import dev.gr1ff3n.mcnmt.domain.model.TripSource
import java.time.Duration
import java.time.ZoneId

/**
 * Pure-functional detection state machine.
 *
 * Given the current [State] and an [Event], returns a new [State] plus a
 * list of side-effect [Command]s. No Android dependencies, no mutable
 * state, fully unit-testable on the JVM.
 */
object TripStateMachine {

    data class Step(val newState: State, val commands: List<Command>)

    private val Empty = emptyList<Command>()

    fun step(
        state: State,
        event: Event,
        autoDetectionEnabled: Boolean = true,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Step = when (state) {
        is State.Idle -> handleIdle(event, autoDetectionEnabled, zone)
        is State.Arming -> handleArming(state, event, autoDetectionEnabled, zone)
        is State.Tracking -> handleTracking(state, event, zone)
    }

    private fun handleIdle(
        event: Event,
        autoEnabled: Boolean,
        zone: ZoneId,
    ): Step = when (event) {
        is Event.ManualStart -> Step(
            newState = State.Tracking(
                source = TripSource.MANUAL,
                startedAtUtc = event.timestampUtc,
                lastPoint = null,
                sustainedBelowSpeedSinceUtc = null,
            ),
            commands = listOf(
                Command.StartGps,
                Command.CreateTrip(TripSource.MANUAL, event.timestampUtc, firstPoint = null),
            ),
        )

        is Event.ActivityRecognized -> {
            val shouldArm = autoEnabled &&
                event.isInVehicle &&
                event.confidence >= DetectionConstants.AR_CONFIDENCE_FLOOR &&
                WorkHours.isWithinWorkHours(event.timestampUtc, zone)
            if (shouldArm) {
                Step(
                    newState = State.Arming(
                        startedAtUtc = event.timestampUtc,
                        firstPoint = null,
                        sustainedAboveSpeedSinceUtc = null,
                    ),
                    commands = listOf(Command.StartGps),
                )
            } else {
                Step(State.Idle, Empty)
            }
        }

        // No-ops in IDLE
        is Event.LocationUpdate,
        is Event.ManualStop,
        is Event.Tick,
        is Event.AutoDetectionToggled -> Step(State.Idle, Empty)
    }

    private fun handleArming(
        state: State.Arming,
        event: Event,
        autoEnabled: Boolean,
        zone: ZoneId,
    ): Step = when (event) {
        is Event.ManualStart -> Step(
            // user took over while we were arming → escalate to MANUAL TRACKING
            newState = State.Tracking(
                source = TripSource.MANUAL,
                startedAtUtc = event.timestampUtc,
                lastPoint = null,
                sustainedBelowSpeedSinceUtc = null,
            ),
            commands = listOf(
                Command.DiscardArmingBuffer,
                Command.CreateTrip(TripSource.MANUAL, event.timestampUtc, firstPoint = null),
            ),
        )

        is Event.ManualStop -> Step(
            newState = State.Idle,
            commands = listOf(Command.DiscardArmingBuffer, Command.StopGps),
        )

        is Event.LocationUpdate -> {
            val currentSpeed = event.speedMps ?: 0f
            val isFast = currentSpeed >= DetectionConstants.START_SPEED_THRESHOLD_MPS
            val sustainedSince = when {
                !isFast -> null
                state.sustainedAboveSpeedSinceUtc == null -> event.timestampUtc
                else -> state.sustainedAboveSpeedSinceUtc
            }

            val confirmed = sustainedSince != null &&
                Duration.between(sustainedSince, event.timestampUtc) >=
                DetectionConstants.START_SPEED_DURATION

            val firstPoint = state.firstPoint ?: PointSummary(
                event.timestampUtc, event.latitude, event.longitude, event.speedMps,
            )

            if (confirmed) {
                Step(
                    newState = State.Tracking(
                        source = TripSource.AUTO,
                        startedAtUtc = state.startedAtUtc,
                        lastPoint = firstPoint,
                        sustainedBelowSpeedSinceUtc = null,
                    ),
                    commands = listOf(
                        Command.CreateTrip(
                            source = TripSource.AUTO,
                            startTimeUtc = state.startedAtUtc,
                            firstPoint = firstPoint,
                        ),
                    ),
                )
            } else {
                Step(
                    newState = state.copy(
                        firstPoint = firstPoint,
                        sustainedAboveSpeedSinceUtc = sustainedSince,
                    ),
                    commands = Empty,
                )
            }
        }

        is Event.Tick -> {
            val expired = Duration.between(state.startedAtUtc, event.timestampUtc) >=
                DetectionConstants.ARMING_TIMEOUT
            if (expired) {
                Step(
                    newState = State.Idle,
                    commands = listOf(Command.DiscardArmingBuffer, Command.StopGps),
                )
            } else {
                Step(state, Empty)
            }
        }

        is Event.ActivityRecognized -> {
            val confidentlyNotInVehicle =
                !event.isInVehicle && event.confidence >= DetectionConstants.AR_CONFIDENCE_FLOOR
            if (confidentlyNotInVehicle) {
                Step(
                    newState = State.Idle,
                    commands = listOf(Command.DiscardArmingBuffer, Command.StopGps),
                )
            } else {
                Step(state, Empty)
            }
        }

        is Event.AutoDetectionToggled -> {
            if (!event.enabled) {
                Step(State.Idle, listOf(Command.DiscardArmingBuffer, Command.StopGps))
            } else {
                Step(state, Empty)
            }
        }
    }

    private fun handleTracking(
        state: State.Tracking,
        event: Event,
        zone: ZoneId,
    ): Step = when (event) {
        is Event.ManualStop -> Step(
            newState = State.Idle,
            commands = listOf(
                Command.EndTrip(
                    endTimeUtc = event.timestampUtc,
                    endSource = TripEndSource.MANUAL_STOP,
                    endPoint = state.lastPoint,
                ),
                Command.StopGps,
            ),
        )

        is Event.LocationUpdate -> {
            val (newLastPoint, addCmd) = applyPoint(state, event)

            val newBelowSince: java.time.Instant? = if (state.source != TripSource.AUTO) {
                null
            } else {
                val currentSpeed = event.speedMps ?: 0f
                val isSlow = currentSpeed < DetectionConstants.STOP_SPEED_THRESHOLD_MPS
                when {
                    !isSlow -> null
                    state.sustainedBelowSpeedSinceUtc == null -> event.timestampUtc
                    else -> state.sustainedBelowSpeedSinceUtc
                }
            }

            val autoStop = newBelowSince != null &&
                Duration.between(newBelowSince, event.timestampUtc) >=
                DetectionConstants.STOP_DURATION

            if (autoStop) {
                Step(
                    newState = State.Idle,
                    commands = listOfNotNull(
                        addCmd,
                        Command.EndTrip(
                            endTimeUtc = event.timestampUtc,
                            endSource = TripEndSource.AUTO_TIMEOUT,
                            endPoint = newLastPoint,
                        ),
                        Command.StopGps,
                    ),
                )
            } else {
                Step(
                    newState = state.copy(
                        lastPoint = newLastPoint,
                        sustainedBelowSpeedSinceUtc = newBelowSince,
                    ),
                    commands = listOfNotNull(addCmd),
                )
            }
        }

        is Event.Tick -> {
            // AUTO trips end when the work-hours window closes. MANUAL trips keep going.
            if (state.source == TripSource.AUTO &&
                !WorkHours.isWithinWorkHours(event.timestampUtc, zone)
            ) {
                Step(
                    newState = State.Idle,
                    commands = listOf(
                        Command.EndTrip(
                            endTimeUtc = event.timestampUtc,
                            endSource = TripEndSource.WORK_HOURS_ENDED,
                            endPoint = state.lastPoint,
                        ),
                        Command.StopGps,
                    ),
                )
            } else {
                Step(state, Empty)
            }
        }

        // Already tracking → ignore further AR + ManualStart + AutoToggle
        is Event.ActivityRecognized,
        is Event.ManualStart,
        is Event.AutoDetectionToggled -> Step(state, Empty)
    }

    private fun applyPoint(
        state: State.Tracking,
        event: Event.LocationUpdate,
    ): Pair<PointSummary, Command.AddPoint?> {
        if (state.lastPoint == null) {
            // First point of a manual trip — accept unconditionally so the
            // map at least has a starting fix.
            val first = PointSummary(
                event.timestampUtc, event.latitude, event.longitude, event.speedMps,
            )
            return first to Command.AddPoint(first, event.accuracyMeters, deltaMeters = 0.0)
        }
        val accept = DistanceCalc.shouldAcceptForDistance(
            prevLat = state.lastPoint.latitude,
            prevLon = state.lastPoint.longitude,
            newLat = event.latitude,
            newLon = event.longitude,
            newAccuracyMeters = event.accuracyMeters,
        )
        return when (accept) {
            is DistanceCalc.Acceptance.Accepted -> {
                val newPoint = PointSummary(
                    event.timestampUtc, event.latitude, event.longitude, event.speedMps,
                )
                newPoint to Command.AddPoint(newPoint, event.accuracyMeters, accept.deltaMeters)
            }
            is DistanceCalc.Acceptance.RejectedAccuracy,
            is DistanceCalc.Acceptance.RejectedTooSmall -> state.lastPoint to null
        }
    }
}
