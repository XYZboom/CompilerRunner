plugins {
    kotlin("jvm")
    application
}

group = "io.github.xyzboom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val cliktVersion: String by properties

dependencies {
    compileOnly(kotlin("compiler", "2.2.21"))
    implementation("com.github.ajalt.clikt:clikt:${cliktVersion}")
    implementation(project(":api"))
    implementation(project(":common-utils"))
    runtimeOnly(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.github.xyzboom.comprun.kotlin.KotlinCompilerProvider"
}