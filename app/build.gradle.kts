import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Private signing key (a gitignored keystore.properties, written by CI from the repo secrets) wins; otherwise the PUBLIC dev key in
// signing/dev/ is used so APKs built by ANY future session or CI run are update-compatible. See signing/README.md.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.tj.optimizer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tj.optimizer"
        minSdk = 29            // the tablet is Android 10 (API 29); anything newer must be guarded with Build.VERSION.SDK_INT
        targetSdk = 29         // = the real device's runtime, so no newer-Android behaviour is assumed (BRIEF.md); also keeps every package visible on Android 11+
        versionCode = 2
        versionName = "0.2.0"
    }

    signingConfigs {
        create("optimizer") {
            storeFile = rootProject.file(keystoreProps.getProperty("storeFile") ?: "signing/dev/optimizer-dev.jks")
            storePassword = keystoreProps.getProperty("storePassword") ?: "optimizer-dev"
            keyAlias = keystoreProps.getProperty("keyAlias") ?: "optimizer-dev"
            keyPassword = keystoreProps.getProperty("keyPassword") ?: "optimizer-dev"
            enableV2Signing = true
            enableV3Signing = true   // allows a later key rotation without uninstalling
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"   // a debug build installs BESIDE the release app
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("optimizer")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += setOf(
                "kotlin/**",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                "META-INF/com.android.tools/**",
                "DebugProbesKt.bin",
            )
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        lintConfig = file("lint.xml")
        // Pinned toolchain (BRIEF.md): upgrades are deliberate, not lint-driven.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion")
        // Play Store rule only: this app is sideloaded onto a fixed Android 10 device and never published, and targetSdk 29 is
        // deliberate (BRIEF.md). Remove this if that ever changes.
        disable += "ExpiredTargetSdkVersion"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions",
        )
    }
}

dependencies {
    // Kotlin stdlib comes with the plugin. NO other runtime dependency without a reason written in BRIEF.md (every one costs APK size, dex methods and RAM).
    testImplementation(libs.junit)
}
