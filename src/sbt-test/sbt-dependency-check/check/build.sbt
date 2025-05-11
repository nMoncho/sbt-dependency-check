import net.nmoncho.sbt.dependencycheck.settings.*
import sbt.complete.DefaultParsers.*
import sbt.*

import java.io.FilenameFilter

version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.13.16"

lazy val checkAddedSuppressions: InputKey[Unit] = inputKey(
  "Checks that suppressions are added to the engine"
)

checkAddedSuppressions := {
  def assert(cond: Boolean, msg: => String): Unit =
    if (!cond) sys.error(msg)

  val count = (Space ~> charClass(_.isDigit, "digit").+).map(_.mkString.toInt).parsed

  val logFiles = (target.value / "global-logging").listFiles(new FilenameFilter {
    override def accept(dir: File, name: String) = name.endsWith("log")
  }).headOption

  logFiles match {
    case Some(logfile) =>
      scala.util.Using(scala.io.Source.fromFile(logfile)) { logs =>
        val lines = logs.getLines().mkString("\n")

        assert(lines.contains(s"Adding [$count] suppression rules to Owasp Engine"), s"Expected [$count] suppressions")
      }.get

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
