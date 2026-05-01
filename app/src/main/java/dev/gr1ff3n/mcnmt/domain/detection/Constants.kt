package dev.gr1ff3n.mcnmt.domain.detection

import java.time.Duration

/**
 * Detection thresholds — locked for v1 per `01-auto-tracking-spec.md`.
 * Surface as Settings in Step 6 if Matt wants to tune after observing
 * real-world behavior.
 */
object DetectionConstants {
    /** 10 mph in m/s. Speed threshold to confirm an auto trip is real. */
    const val START_SPEED_THRESHOLD_MPS: Double = 4.4704

    /** Sustained-above-speed duration before an auto trip is confirmed. */
    val START_SPEED_DURATION: Duration = Duration.ofSeconds(30)

    /** 2 mph in m/s. "Effectively stopped" — covers idle GPS jitter. */
    const val STOP_SPEED_THRESHOLD_MPS: Double = 0.8941

    /** Sustained-below-speed duration before an auto trip auto-ends. */
    val STOP_DURATION: Duration = Duration.ofMinutes(10)

    /** Time we keep GPS on after AR trigger before giving up on confirmation. */
    val ARMING_TIMEOUT: Duration = Duration.ofMinutes(5)

    /** Drop GPS fixes worse than this — first-fix garbage usually exceeds 30m. */
    const val ACCURACY_FILTER_METERS: Double = 30.0

    /** Drop deltas below this from distance accumulation (parked-engine wobble). */
    const val MIN_STEP_METERS: Double = 10.0

    /** Ignore Activity Recognition transitions below this confidence (0-100). */
    const val AR_CONFIDENCE_FLOOR: Int = 75

    /** Local-time work hours. Auto-detection ignored outside this window. */
    const val WORK_HOUR_START: Int = 9
    const val WORK_HOUR_END: Int = 18
}
