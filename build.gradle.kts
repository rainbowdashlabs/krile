import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
}

group = "de.chojo"
version = "1.1.2"

repositories {
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public/")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

dependencies {
    implementation(libs.jgit)
    implementation(libs.cjda.util) {
        exclude(group = "club.minnced", module = "opus-java")
    }

    implementation(libs.jackson.yaml)
    compileOnly(libs.jetbrains.annotations)

    implementation(libs.postgresql)
    implementation(libs.bundles.sadu)

    implementation(libs.bundles.log4j)
    implementation(libs.log.util) {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
    testCompileOnly(libs.jetbrains.annotations)
}

tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                expand(
                        "version" to project.version
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.krile.Krile"))
        }
    }

    build {
        dependsOn(spotlessApply)
        dependsOn(shadowJar)
    }
}
