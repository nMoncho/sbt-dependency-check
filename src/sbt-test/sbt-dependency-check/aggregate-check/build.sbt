import net.nmoncho.sbt.dependencycheck.settings.*
import sbt.Keys.libraryDependencies
import sbt.io.Using

import java.io.FilenameFilter

lazy val commonSettings = Seq(
  organization := "net.nmoncho",
  version := "0.1.0",
  scalaVersion := "2.13.15"
)

ThisBuild / dependencyCheckDataDirectory := scala.util.Properties
  .envOrNone("DATA_DIRECTORY")
  .map(new File(_))
ThisBuild / dependencyCheckNvdApi := sys.env
  .get("NVD_API_KEY")
  .map(key => NvdApiSettings(key))
  .getOrElse(NvdApiSettings.Default)

lazy val root = (project in file("."))
  .aggregate(core)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.eclipse.jetty" % "jetty-runner" % "9.2.4.v20141103" % "provided",
    libraryDependencies += "commons-collections" % "commons-collections" % "3.2.1" % "optional",
    dependencyCheckScopes := ScopesSettings(
      test     = false,
      provided = false,
      optional = false
    ),
    dependencyCheckFailBuildOnCVSS := 0,
    TaskKey[Unit]("check") := {
      def assert(cond: Boolean, msg: => String): Unit =
        if (!cond) sys.error(msg)

      val logFiles = (target.value / "global-logging")
        .listFiles(new FilenameFilter {
          override def accept(dir: File, name: String) = name.endsWith("log")
        })
        .headOption

      logFiles match {
        case Some(logfile) =>
          scala.util
            .Using(scala.io.Source.fromFile(logfile)) { logs =>
              val lines = logs.getLines().mkString("\n")

              assert(
                lines.contains("commons-collections4-4.1.jar"),
                "'commons-collections4-4.1.jar' should be scanned since it's coming from 'core'"
              )
              assert(
                !lines.contains("jackson-databind-2.9.9.jar"),
                "'jackson-databind-2.9.9.jar' should be not scanned since it's coming from 'ignore', and it's not aggregated"
              )
            }
            .get

        case None =>
          sys.error("Log file not found, cannot assert check...")
      }

      ()
    }
  )

lazy val util = (project in file("util"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "commons-beanutils"            % "commons-beanutils"   % "1.9.1"         % "test",
      "org.springframework.security" % "spring-security-web" % "5.1.4.RELEASE" % "test"
    )
  )

lazy val core = project
  .dependsOn(util)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-collections4" % "4.1"
  )

lazy val ignore = (project in file("ignore"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9"
  )
