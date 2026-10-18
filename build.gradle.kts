plugins {
    kotlin("jvm") version "2.2.21"
    application
    `maven-publish`
}

group = "io.github.xyzboom"
version = "1.0-SNAPSHOT"

val remoteRepositoryUrl = providers.gradleProperty("mavenRepositoryUrl")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_URL"))
val remoteRepositoryUsername = providers.gradleProperty("mavenRepositoryUsername")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_USERNAME"))
val remoteRepositoryPassword = providers.gradleProperty("mavenRepositoryPassword")
    .orElse(providers.environmentVariable("MAVEN_REPOSITORY_PASSWORD"))

allprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        if (components.findByName("java") != null) {
            publishing {
                publications {
                    register<MavenPublication>("mavenJava") {
                        from(components["java"])
                    }
                }
                remoteRepositoryUrl.orNull?.let { repositoryUrl ->
                    repositories {
                        maven {
                            name = "remote"
                            url = uri(repositoryUrl)
                            val username = remoteRepositoryUsername.orNull
                            val password = remoteRepositoryPassword.orNull
                            if (username != null && password != null) {
                                credentials {
                                    this.username = username
                                    this.password = password
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

val cliktVersion by properties

dependencies {
    api(project(":api"))
    implementation(project(":common-utils"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("ch.epfl.scala:bsp4j:2.2.0-M4.TEST")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.25.3")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.3")
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