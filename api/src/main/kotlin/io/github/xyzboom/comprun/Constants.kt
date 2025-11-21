package io.github.xyzboom.comprun

object Constants {
    const val BUILD_TARGET_NAME = "DefaultBuildTarget"
    const val DEFAULT_SUPPLIER = "Official"
    val CRHome: String = System.getProperty(
        "CR_HOME",
        "${System.getProperty("user.home")}/.cr"
    )
}