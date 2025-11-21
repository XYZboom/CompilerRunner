import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "io.github.xyzboom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val cliktVersion by properties

dependencies {
    api(project(":api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("ch.epfl.scala:bsp4j:2.2.0-M4.TEST")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.20.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    runtimeOnly(project(":kotlin-runner"))
    runtimeOnly(project(":java-runner"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
application {
    mainClass = "io.github.xyzboom.comprun.server.CompRunServer"
}