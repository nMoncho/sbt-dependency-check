import net.nmoncho.sbt.dependencycheck.settings.*
import sbt.*
import sbt.complete.DefaultParsers.*

import java.io.FilenameFilter

version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.13.16"

lazy val checkLogContains: InputKey[Unit] = inputKey(
  "checks that the logs contains a message"
)

checkLogContains := {
  def assert(cond: Boolean, msg: => String): Unit =
    if (!cond) sys.error(msg)

  def assertNot(cond: Boolean, msg: => String): Unit =
    if (cond) sys.error(msg)

  val ((negated, expected), errorMessage) = ((Space ~> '!').?.map(
    _.isDefined
  ) ~ (Space ~> StringEscapable) ~ (Space ~> StringEscapable).?).parsed

  val logFiles = (baseDirectory.value / "target" / "global-logging")
    .listFiles(new FilenameFilter {
      override def accept(dir: File, name: String) = name.endsWith("log")
    })
    .headOption

  logFiles match {
    case Some(logfile) =>
      scala.util
        .Using(scala.io.Source.fromFile(logfile)) { logs =>
          val lines = logs
            .getLines()
            .filterNot(
              _.contains("checkLogContains")
            ) // remove 'checkLogContains' invoke to avoid false positives
            .mkString("\n")

          val sert = if (negated) {
            assertNot(_, errorMessage.getOrElse(s"Found [$expected] in logs but didn't expect to"))
          } else {
            assert(_, errorMessage.getOrElse(s"Couldn't find [$expected] in logs"))
          }

          sert(lines.contains(expected))
        }
        .get

    case None =>
      sys.error("Log file not found, cannot assert check...")
  }

  ()
}

lazy val checkAddedSuppressions: InputKey[Unit] = inputKey(
  "Checks that suppressions are added to the engine"
)

checkAddedSuppressions := {
  def assert(cond: Boolean, msg: => String): Unit =
    if (!cond) sys.error(msg)

  val count = (Space ~> charClass(_.isDigit, "digit").+).map(_.mkString.toInt).parsed

  val logFiles = (baseDirectory.value / "target" / "global-logging")
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
            lines.contains(s"Adding [$count] suppression rules to Owasp Engine"),
            s"Expected [$count] suppressions"
          )
        }
        .get

    case None =>
      sys.error("Log file not found, cannot assert check...")
  }

  ()
}

libraryDependencies ++= Seq(
  "commons-beanutils"       % "commons-beanutils"    % "1.9.1"           % "test",
  "org.eclipse.jetty"       % "jetty-runner"         % "9.2.4.v20141103" % "provided",
  "com.github.t3hnar"       % "scala-bcrypt_2.10"    % "2.6"             % "runtime",
  "org.apache.commons"      % "commons-collections4" % "4.1",
  "com.google.oauth-client" % "google-oauth-client"  % "1.22.0"          % "optional"
)

dependencyCheckScopes := ScopesSettings(
  test     = true,
  provided = false,
  runtime  = true
)

ThisBuild / dependencyCheckDataDirectory := scala.util.Properties
  .envOrNone("DATA_DIRECTORY")
  .map(new File(_))
ThisBuild / dependencyCheckNvdApi := sys.env
  .get("NVD_API_KEY")
  .map(key => NvdApiSettings(key))
  .getOrElse(NvdApiSettings.Default)
