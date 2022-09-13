package net.snipsniper.utils

enum class ClockDirection {
    CLOCKWISE, COUNTERCLOCKWISE
}

enum class ConfigSaveButtonState {
    UPDATE_CLEAN_STATE, NO_SAVE, YES_SAVE
}

enum class LaunchType {
    NORMAL, EDITOR, VIEWER
}

enum class PlatformType {
    JAR, WIN, WIN_INSTALLED, STEAM, UNKNOWN
}

enum class ReleaseType {
    STABLE, DEV, DIRTY, UNKNOWN
}