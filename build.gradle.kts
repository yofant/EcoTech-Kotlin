plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.findByName("detekt")?.let { detektExt ->
        try {
            val setBuildUponDefaultConfig = detektExt.javaClass.getMethod("setBuildUponDefaultConfig", Boolean::class.javaPrimitiveType)
            val setAllRules = detektExt.javaClass.getMethod("setAllRules", Boolean::class.javaPrimitiveType)
            setBuildUponDefaultConfig.invoke(detektExt, true)
            setAllRules.invoke(detektExt, false)
        } catch (_: Exception) {
        }
    }
}
