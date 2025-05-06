import net.nmoncho.sbt.dependencycheck.settings._

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
    dependencyCheckFailBuildOnCVSS := 0
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
