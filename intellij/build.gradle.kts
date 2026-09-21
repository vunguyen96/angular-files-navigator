plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.vna.angularfilesnavigator"
version = "1.1.0"

repositories {
    mavenCentral()
}

// Compile against IntelliJ IDEA Community; the plugin uses only core platform
// APIs, so the resulting artifact runs in IDEA, WebStorm, PhpStorm, etc.
intellij {
    val ideLocalPath = System.getenv("IDE_LOCAL_PATH") ?: findProperty("ideLocalPath") as String?
    if (ideLocalPath.isNullOrBlank()) {
        version.set("2023.3.6")
        type.set("IC")
    } else {
        // Build against an already-downloaded IDE to avoid re-downloading the SDK.
        localPath.set(ideLocalPath)
    }
    plugins.set(emptyList())
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        // No upper bound: stay compatible with current and future IDE builds.
        untilBuild.set(provider { null })
    }

    // Speeds up the build; not needed for a keymap/navigation plugin.
    buildSearchableOptions {
        enabled = false
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
