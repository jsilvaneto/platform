plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val versionFile = rootProject.file("VERSION")
val appVersionName = if (versionFile.exists()) versionFile.readText().trim() else "1.2.0"
val versionParts = appVersionName.split(".").mapNotNull { it.toIntOrNull() }
val appVersionCode = if (versionParts.size >= 3) {
    versionParts[0] * 10000 + versionParts[1] * 100 + versionParts[2]
} else {
    10200
}

android {
    namespace = "com.platform.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.platform.app"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Biometria e Segurança do Dispositivo
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)

    // Injeção de Dependências com Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Persistência Local com Room e DataStore
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Rede e APIs com Retrofit / OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp.logging)

    // Coroutines Assíncronas
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testes Unitários
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

    // Testes de Instrumentação
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register("checkLiteralColors") {
    group = "verification"
    description = "Checks that no literal colors (Color(0x...), Color.White, Color.Black, etc.) are used outside presentation/theme"
    doLast {
        val presentationDir = file("src/main/java/com/platform/app/presentation")
        val themeDir = file("src/main/java/com/platform/app/presentation/theme")
        val forbiddenPatterns = listOf(
            Regex("""\bColor\(0x[0-9a-fA-F]+\)"""),
            Regex("""\bColor\.(White|Black|Red|Green|Blue|Yellow|Cyan|Magenta|Gray|DarkGray|LightGray)\b""")
        )

        val violations = mutableListOf<String>()

        presentationDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt" && !file.startsWith(themeDir)) {
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                            for (pattern in forbiddenPatterns) {
                                if (pattern.containsMatchIn(line)) {
                                    val relativePath = file.relativeTo(projectDir).path.replace('\\', '/')
                                    violations.add("$relativePath:${index + 1}: $trimmed")
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            val message = buildString {
                appendLine("FAILED: Encontradas ${violations.size} violação(ões) de cor literal fora de presentation/theme:")
                violations.forEach { appendLine("  - $it") }
                appendLine("\nRegra Arquitetural (coding_standards.md):")
                appendLine("Cores literais (Color(0x...), Color.White, Color.Black, Color.Gray, etc.) são proibidas fora do pacote theme.")
                appendLine("Utilize MaterialTheme.colorScheme.* ou tokens em PlatformColorPalette / ThemePreviewColors / CardSkinColors.")
            }
            throw GradleException(message)
        } else {
            println("checkLiteralColors: Todas as telas respeitam estritamente os tokens de tema (0 violações encontradas).")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkLiteralColors")
}

