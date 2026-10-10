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
    sourceSets {
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
        getByName("test").assets.srcDirs("$projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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
    testImplementation(libs.androidx.room.testing)

    // Testes de Instrumentação
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

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

tasks.register("checkHardcodedStrings") {
    group = "verification"
    description = "Checks that no new hardcoded string literals Text(\"...\") are introduced in presentation outside the baseline"
    doLast {
        val presentationDir = file("src/main/java/com/platform/app/presentation")
        val baselineFile = rootProject.file("config/hardcoded-strings-baseline.txt")
        val hardcodedPattern = Regex("""\bText\s*\(\s*"([^"]+)"""")

        val baselineEntries = if (baselineFile.exists()) {
            baselineFile.readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { line ->
                    val parts = line.split(": ", limit = 2)
                    val filePath = parts[0].substringBeforeLast(":")
                    val content = if (parts.size > 1) parts[1] else ""
                    "$filePath::: $content"
                }
                .toSet()
        } else {
            emptySet()
        }

        val currentViolations = mutableListOf<String>()

        presentationDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                            if (hardcodedPattern.containsMatchIn(line)) {
                                val relativePath = file.relativeTo(projectDir).path.replace('\\', '/')
                                val key = "$relativePath::: $trimmed"
                                if (key !in baselineEntries) {
                                    currentViolations.add("$relativePath:${index + 1}: $trimmed")
                                }
                            }
                        }
                    }
                }
            }
        }

        val newViolations = currentViolations

        if (newViolations.isNotEmpty()) {
            val message = buildString {
                appendLine("FAILED: Encontradas ${newViolations.size} nova(s) string(s) literais Text(\"...\") fora da baseline:")
                newViolations.forEach { appendLine("  - $it") }
                appendLine("\nRegra Arquitetural (coding_standards.md):")
                appendLine("Strings literais em componentes Text(\"...\") são proibidas para novos componentes.")
                appendLine("Mova os textos para AppStrings ou strings.xml.")
            }
            throw GradleException(message)
        } else {
            println("checkHardcodedStrings: Nenhuma nova string hardcoded introduzida (${currentViolations.size} na baseline legada).")
        }
    }
}

tasks.register("checkFileSize") {
    group = "verification"
    description = "Checks that presentation, data, and domain Kotlin files respect the maximum limit of ~600 lines"
    doLast {
        val targetDirs = listOf(
            file("src/main/java/com/platform/app/presentation"),
            file("src/main/java/com/platform/app/data"),
            file("src/main/java/com/platform/app/domain")
        )
        val baselineFile = rootProject.file("config/file-size-baseline.txt")
        val maxLines = 600

        val baselineFiles = if (baselineFile.exists()) {
            baselineFile.readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .map { it.substringBefore(":") }
                .toSet()
        } else {
            emptySet()
        }

        val violations = mutableListOf<String>()

        targetDirs.forEach { dir ->
            if (dir.exists()) {
                dir.walkTopDown().forEach { file ->
                    if (file.isFile && file.extension == "kt") {
                        val lineCount = file.readLines().size
                        if (lineCount > maxLines) {
                            val relativePath = file.relativeTo(projectDir).path.replace('\\', '/')
                            if (relativePath !in baselineFiles) {
                                violations.add("$relativePath ($lineCount linhas > $maxLines)")
                            }
                        }
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            val message = buildString {
                appendLine("FAILED: Encontrado(s) ${violations.size} arquivo(s) em presentation/, data/ ou domain/ excedendo o limite de $maxLines linhas:")
                violations.forEach { appendLine("  - $it") }
                appendLine("\nRegra Arquitetural (AGENT_RULES.md / coding_standards.md):")
                appendLine("Máximo de ~600 linhas por arquivo em presentation, data e domain.")
                appendLine("Decomponha o arquivo extraindo componentes ou agregados especializados.")
            }
            throw GradleException(message)
        } else {
            println("checkFileSize: Todos os arquivos em presentation/, data/ e domain/ respeitam o teto de $maxLines linhas (baseline ativa para legado).")
        }
    }
}

tasks.register("checkRawShapes") {
    group = "verification"
    description = "Checks that no raw RoundedCornerShape are used outside presentation/theme, enforcing PlatformShapes"
    doLast {
        val presentationDir = file("src/main/java/com/platform/app/presentation")
        val themeDir = file("src/main/java/com/platform/app/presentation/theme")
        val baselineFile = rootProject.file("config/raw-shapes-baseline.txt")
        val rawShapePattern = Regex("""\bRoundedCornerShape\(""")

        val baselineEntries = if (baselineFile.exists()) {
            baselineFile.readLines()
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("#") }
                .toSet()
        } else {
            emptySet()
        }

        val currentViolations = mutableListOf<String>()

        presentationDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt" && !file.startsWith(themeDir) && file.name != "PlatformShapes.kt") {
                file.useLines { lines ->
                    lines.forEachIndexed { index, line ->
                        val trimmed = line.trim()
                        if (!trimmed.startsWith("//") && !trimmed.startsWith("/*") && !trimmed.startsWith("*")) {
                            if (rawShapePattern.containsMatchIn(line)) {
                                val relativePath = file.relativeTo(projectDir).path.replace('\\', '/')
                                currentViolations.add("$relativePath:${index + 1}: $trimmed")
                            }
                        }
                    }
                }
            }
        }

        val newViolations = currentViolations.filterNot { it in baselineEntries }

        if (newViolations.isNotEmpty()) {
            val message = buildString {
                appendLine("FAILED: Encontradas ${newViolations.size} nova(s) forma(s) RoundedCornerShape cruas fora de presentation/theme:")
                newViolations.forEach { appendLine("  - $it") }
                appendLine("\nRegra de Design System:")
                appendLine("Formas cruas RoundedCornerShape(...) são proibidas na presentation fora de theme/.")
                appendLine("Utilize tokens semânticos de PlatformShapes (small, medium, large, pill).")
            }
            throw GradleException(message)
        } else {
            println("checkRawShapes: Todas as telas respeitam os tokens de PlatformShapes (${currentViolations.size} na baseline).")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("checkLiteralColors", "checkHardcodedStrings", "checkFileSize", "checkRawShapes")
}

