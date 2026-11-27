@file:Suppress("UnstableApiUsage")

val useMavenLocalInnerTubeX = providers.gradleProperty("useMavenLocalInnerTubeX").isPresent

pluginManagement {
    repositories {
        mavenLocal()
        // Authoritative sources only — see the note in dependencyResolutionManagement for why no
        // third-party mirror is listed.
        //
        // The Plugin Portal must come before a generic Maven repo: marker plugins such as
        // `org.jetbrains.kotlin.plugin.serialization` resolve through a marker POM that the Portal
        // knows how to map onto its implementation artifact.
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Authoritative repositories only. No third-party mirror.
        //
        // A mirror was tried here to dodge Maven Central's HTTP 429, and it made things strictly
        // worse: `maven.aliyun.com/repository/public` returns **502 Bad Gateway** for paths it does
        // not proxy (verified against the KSP plugin marker), and because Gradle disables a
        // repository after an error, one 502 became "61 more failures with identical causes".
        //
        // A mirror can only ever be a latency optimisation; it must never be the first place Gradle
        // looks, because its failure modes (502, HTML error pages, empty 200s) are indistinguishable
        // from "artifact does not exist". Google and Central are the sources of truth for everything
        // this project uses, and the retry/back-off settings in gradle.properties handle the 429
        // case by waiting it out instead of routing around it.
        google()
        mavenCentral()

        // `com.github.promeg:tinypinyin` is NOT on Maven Central (verified: 404) and is not on
        // JitPack under that casing either. A mirror is the only source, so it has to be listed.
        //
        // Scoped to exactly the groups that need it. That is the important part: an unscoped mirror
        // was previously first in this list and returned 502 for paths it does not proxy, which
        // disabled the repository for the rest of the build and failed every subsequent artifact.
        // Scoping keeps it from ever being consulted for anything else, and because it is last,
        // Google and Central answer first for everything they carry.
        maven("https://maven.aliyun.com/repository/public") {
            content {
                // Only tinypinyin is missing from Central; its `org.ahocorasick` dependency IS on
                // Central, so it is deliberately not listed here.
                includeGroup("com.github.promeg")
            }
        }

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

        // JitPack hosts the GitHub-hosted deps (ucrop, tinypinyin, …).
        //
        // Unscoped on purpose. Gradle matches `content { includeGroupByRegex }` filters against a
        // normalised group string while JitPack serves case-sensitive paths, so a filter can make
        // JitPack be consulted for a coordinate whose path differs only by case. Cheap to leave
        // open: JitPack is last in this list and returns 404 for what it does not host.
        maven("https://jitpack.io")
    }
}

// F-Droid doesn't support foojay-resolver plugin
rootProject.name = "Metrolist"
include(":app")
include(":innertube")
