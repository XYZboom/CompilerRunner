plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "CompilerRunner"
include("kotlin-runner")
include("api")
include("client")
include("common-utils")
include("java-runner")