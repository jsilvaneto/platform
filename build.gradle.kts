plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

allprojects {
    val localBuildDir = File(System.getProperty("user.home"), ".gradle-builds/platform/${project.name}")
    layout.buildDirectory.set(localBuildDir)
}

