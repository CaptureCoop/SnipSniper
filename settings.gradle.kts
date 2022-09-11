rootProject.name = "SnipSniper"
sourceControl {
    gitRepository(uri("https://github.com/CaptureCoop/CCUtils.git")) {
        producesModule("org.capturecoop:CCUtils")
    }

    gitRepository(uri("https://github.com/CaptureCoop/CCLogger.git")) {
        producesModule("org.capturecoop:CCLogger")
    }

    gitRepository(uri("https://github.com/CaptureCoop/CCColorUtils.git")) {
        producesModule("org.capturecoop:CCColorUtils")
    }
}