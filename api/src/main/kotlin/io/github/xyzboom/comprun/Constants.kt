package io.github.xyzboom.comprun

object Constants {
    const val BUILD_TARGET_NAME = "DefaultBuildTarget"
    val CRHome: String = System.getProperty(
        "CR_HOME",
        "${System.getProperty("user.home")}/.cr"
    )
}