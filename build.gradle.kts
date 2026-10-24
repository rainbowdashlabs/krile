import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    java
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
}

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
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat(libs.versions.palantir.get()).formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        encoding("UTF-8")
    }

    format("locales") {
        target("src/main/resources/locale*.properties")
        encoding("UTF-8")
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
        val version = project.version.toString()
        inputs.property("version", version)
        filesMatching("version") {
            expand("version" to version)
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
        dependsOn(shadowJar)
    }
}
