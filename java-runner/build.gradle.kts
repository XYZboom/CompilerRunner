plugins {
    kotlin("jvm")
}

group = "io.github.xyzboom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val cliktVersion: String by properties

dependencies {
    compileOnly("org.eclipse.jdt.core.compiler:ecj:3.7")
    implementation("com.github.ajalt.clikt:clikt:${cliktVersion}")
    implementation(project(":api"))
    implementation(project(":common-utils"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}