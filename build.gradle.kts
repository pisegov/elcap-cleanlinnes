plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("kapt") version "1.9.0"
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

    implementation("com.google.dagger:dagger:2.47")
    kapt("com.google.dagger:dagger-compiler:2.47")
//
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("com.mysql:mysql-connector-j:8.0.33")
//    implementation("mysql:mysql-connector-java:8.0.33")
//    implementation("com.mysql:mysql-connector-j:8.1.0")

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