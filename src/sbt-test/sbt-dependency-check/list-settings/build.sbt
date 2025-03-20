import net.nmoncho.sbt.dependencycheck.settings._

version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.13.16"

dependencyCheckNvdApi := sys.env.get("NVD_API_KEY").map(key => NvdApiSettings(key)).getOrElse(NvdApiSettings.Default)
