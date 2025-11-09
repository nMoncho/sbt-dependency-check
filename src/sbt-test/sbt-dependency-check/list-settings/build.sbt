import net.nmoncho.sbt.dependencycheck.settings._

ThisBuild / dependencyCheckDataDirectory := scala.util.Properties
  .envOrNone("DATA_DIRECTORY")
  .map(new File(_))
ThisBuild / dependencyCheckNvdApi := sys.env
  .get("NVD_API_KEY")
  .map(key => NvdApiSettings(key))
  .getOrElse(NvdApiSettings.Default)

lazy val commonSettings = Seq(
  organization := "net.nmoncho",
  version := "0.1.0",
  scalaVersion := "2.13.15"
)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings)
  .settings(
    dependencyCheckFailBuildOnCVSS := 0
  )

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1",
    dependencyCheckSuppressions := SuppressionSettings(
      suppressions = Seq(
        SuppressionRule(cvssBelow = Seq(1.0))
      )
    )
  )

lazy val inScope = (project in file("inScope"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9",
    dependencyCheckSuppressions := SuppressionSettings(
      suppressions = Seq(
        SuppressionRule(cvssBelow = Seq(2.0))
      )
    )
  )
