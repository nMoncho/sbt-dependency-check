import Dependencies.*

ThisBuild / organization := "net.nmoncho"

addCommandAlias(
  "testCoverage",
  "; clean ; coverage; test; coverageAggregate; coverageReport; coverageOff"
)

addCommandAlias(
  "styleFix",
  "; scalafmtSbt; +scalafmtAll; +headerCreateAll; scalafixAll"
)

addCommandAlias(
  "styleCheck",
  "; +scalafmtCheckAll; +headerCheckAll; scalafixAll --check"
)

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-dependency-check",
    startYear := Some(2025),
    homepage := Some(url("https://github.com/nMoncho/sbt-dependency-check")),
    licenses := Seq("MIT License" -> new URL("http://opensource.org/licenses/MIT")),
    headerLicense := Some(HeaderLicense.MIT("2025", "the original author or authors")),
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository := "https://s01.oss.sonatype.org/service/local",
    developers := List(
      Developer(
        "nMoncho",
        "Gustavo De Micheli",
        "gustavo.demicheli@gmail.com",
        url("https://github.com/nMoncho")
      )
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions := (Opts.compile.encoding("UTF-8") :+
      Opts.compile.deprecation :+
      Opts.compile.unchecked :+
      "-feature" :+
      "-Ywarn-unused"),
    libraryDependencies ++= Seq(
      dependencyCheck,
      munit           % Test,
      munitScalaCheck % Test,
      // log4jSf4jImpl   % Test,
      mockito % Test
    ),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        // set minimum sbt version so we have `sbtPluginPublishLegacyMavenStyle`
        case "2.12" => "1.9.0"
      }
    },
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    Test / testOptions += Tests.Argument("-F") // Show full stack trace
  )
