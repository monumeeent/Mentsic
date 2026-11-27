@file:Suppress("UnstableApiUsage")

val useMavenLocalInnerTubeX = providers.gradleProperty("useMavenLocalInnerTubeX").isPresent

pluginManagement {
    repositories {
        mavenLocal()
        google()
        // Plugin resolution order is the OPPOSITE of dependency resolution (see the block below).
        //
        // Marker plugins such as `org.jetbrains.kotlin.plugin.serialization` are published to the
        // Gradle Plugin Portal, and the Portal is what knows how to redirect a marker id to its
        // implementation artifact. Asking a Maven mirror first adds a failing lookup per plugin and,
        // when the mirror answers oddly, makes Gradle report "plugin was not found" for a plugin
        // that exists on every repository. So: authoritative sources first, mirrors as the last
        // resort for an outage.
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Order matters. Maven Central answers a burst of parallel module requests with HTTP 429
        // (Too Many Requests), and Gradle then disables that repository for the *rest of the build* —
        // turning one 429 into sixty-odd "could not resolve" failures.
        //
        // So: Google first (AndroidX/media3 live only there and it is not rate limited this way),
        // then a Maven Central mirror, then Central itself as the authority, then JitPack. Gradle
        // tries repositories in order and stops at the first hit, so the mirror absorbs the burst
        // and Central is only reached for artifacts the mirror lacks.
        google()

        maven("https://maven.aliyun.com/repository/public")

        mavenCentral()

        exclusiveContent {
            forRepository {
                if (useMavenLocalInnerTubeX) mavenLocal() else maven("https://jitpack.io")
            }
            filter {
                if (useMavenLocalInnerTubeX) {
                    includeModule("com.github.MetrolistGroup", "innertubex")
                    includeModule("com.github.MetrolistGroup", "innertubex-android")
                    includeModule("com.github.MetrolistGroup", "innertubex-desktop")
                } else {
                    includeGroup("com.github.MetrolistGroup.innertubex")
                }
            }
        }

        // JitPack hosts the GitHub-hosted deps (ucrop, tinypinyin, …). Those must never be asked of
        // the mirrors, which would answer 404 and slow every build down.
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
    }
}

// F-Droid doesn't support foojay-resolver plugin
rootProject.name = "Metrolist"
include(":app")
include(":innertube")
