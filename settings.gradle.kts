@file:Suppress("UnstableApiUsage")

val useMavenLocalInnerTubeX = providers.gradleProperty("useMavenLocalInnerTubeX").isPresent

pluginManagement {
    repositories {
        mavenLocal()
        google()
        // Mirror before Maven Central, for the same 429 reason as the dependency repositories below.
        // The Portal and Central stay in the list as the authority.
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
        mavenCentral()
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
