import net.nmoncho.sbt.dependencycheck.settings.*

version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.13.16"

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
