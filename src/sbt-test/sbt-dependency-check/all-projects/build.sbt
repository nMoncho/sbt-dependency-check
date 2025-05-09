import net.nmoncho.sbt.dependencycheck.settings.*

import java.io.FilenameFilter

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
  .settings(commonSettings: _*)
  .settings(
    dependencyCheckFailBuildOnCVSS := 0,
    TaskKey[Unit]("check") := {
      def assert(cond: Boolean, msg: => String): Unit =
        if (!cond) sys.error(msg)

      val logFiles = (target.value / "global-logging").listFiles(new FilenameFilter {
        override def accept(dir: File, name: String) = name.endsWith("log")
      }).headOption

      logFiles match {
        case Some(logfile) =>
          scala.util.Using(scala.io.Source.fromFile(logfile)) { logs =>
            val lines = logs.getLines().mkString("\n")

            assert(lines.contains("commons-collections4-4.1.jar"), "'commons-collections4-4.1.jar' should be scanned since it's coming from 'core'")
            assert(lines.contains("jackson-databind-2.9.9.jar"), "'jackson-databind-2.9.9.jar' should be scanned since it's coming from 'inScope'")
          }.get

        case None =>
          sys.error("Log file not found, cannot assert check...")
      }

      ()
    }
  )

lazy val core = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1",
    dependencyCheckSuppressions := SuppressionSettings(
      suppressions = Seq(
        SuppressionRule(cvssBelow = Seq(1.0))
      )
    )
  )

lazy val inScope = (project in file("inScope"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9",
    dependencyCheckSuppressions := SuppressionSettings(
      suppressions = Seq(
        SuppressionRule(cvssBelow = Seq(2.0))
      )
    )
  )
