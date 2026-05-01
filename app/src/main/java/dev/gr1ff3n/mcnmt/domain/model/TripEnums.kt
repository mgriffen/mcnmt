package dev.gr1ff3n.mcnmt.domain.model

enum class TripSource {
    AUTO,
    MANUAL,
}

enum class TripEndSource {
    AUTO_TIMEOUT,
    MANUAL_STOP,
    WORK_HOURS_ENDED,
    ORPHAN_RECOVERY,
}

enum class TripCategory {
    UNREVIEWED,
    WORK,
    PERSONAL,
    LUNCH,
}
