plugins {
    kotlin("jvm")
}

group = "io.github.xyzboom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val cliktVersion: String by properties
val mavenResolverVersion: String by properties
val ktorVersion: String by properties

dependencies {
    implementation(project(":api"))
    implementation("com.github.ajalt.clikt:clikt:${cliktVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.maven.resolver:maven-resolver-transport-http:1.9.24")
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${ktorVersion}")
    api("org.apache.maven.resolver:maven-resolver-supplier:1.9.24")
    api("org.apache.maven:maven-resolver-provider:3.9.11")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}