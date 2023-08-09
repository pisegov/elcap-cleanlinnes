plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("kapt") version "1.9.0"
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

    implementation("com.google.dagger:dagger:2.47")
    kapt("com.google.dagger:dagger-compiler:2.47")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}