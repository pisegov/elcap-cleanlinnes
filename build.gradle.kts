plugins {
    kotlin("jvm") version "1.9.0"
    application
}


group = "club.elcapitan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val tgBotApiVersion by System.getProperties()
    implementation("dev.inmo:tgbotapi:$tgBotApiVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}