plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.chojo"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
}

java{
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
    implementation("de.chojo", "cjda-util", "2.9.0+beta.11-SNAPSHOT") {
        exclude(group = "club.minnced", module = "opus-java")
    }

    // database
    implementation("org.postgresql", "postgresql", "42.6.0")
    implementation("de.chojo.sadu", "sadu-queries", "1.3.0-SNAPSHOT")
    implementation("de.chojo.sadu", "sadu-updater", "1.3.0-SNAPSHOT")
    implementation("de.chojo.sadu", "sadu-postgresql", "1.3.0-SNAPSHOT")
    implementation("de.chojo.sadu", "sadu-datasource", "1.3.0")

    // Logging
    implementation("org.slf4j", "slf4j-api", "2.0.7")
    implementation("org.apache.logging.log4j", "log4j-core", "2.20.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.20.0")
    implementation("de.chojo", "log-util", "1.0.1") {
        exclude("org.apache.logging.log4j")
    }

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
}

tasks{
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
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.krile.Krile"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
