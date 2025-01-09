plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    application
}


tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}


group = "club.elcapitan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val tgBotApiVersion by System.getProperties()
    val exposedVersion by System.getProperties()

    implementation("dev.inmo:tgbotapi:$tgBotApiVersion")

    implementation("com.google.dagger:dagger:2.54")
    kapt("com.google.dagger:dagger-compiler:2.54")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")

    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}