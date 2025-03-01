rootProject.name = "krile"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // misc
            library("jetbrains-annotations", "org.jetbrains:annotations:24.1.0")
            version("sadu", "2.3.1")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            bundle("sadu", listOf("sadu-queries", "sadu-updater", "sadu-postgresql", "sadu-datasource"))

            version("log4j", "2.24.3")

            library("slf4j-api", "org.slf4j:slf4j-api:2.0.13")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            library("log4j-jsontemplate", "org.apache.logging.log4j", "log4j-layout-template-json").versionRef("log4j")
            bundle("log4j", listOf("slf4j-api", "log4j-core", "log4j-slf4j2", "log4j-jsontemplate"))

            // plugins
            plugin("spotless", "com.diffplug.spotless").version("6.25.0")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")

        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.9.0")
}
