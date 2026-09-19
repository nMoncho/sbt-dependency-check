import Dependencies.*

ThisBuild / organization := "net.nmoncho"

addCommandAlias(
  "testCoverage",
  "; clean ; coverage; +test; +scripted; coverageAggregate; coverageReport; coverageOff"
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
    licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT")),
    headerLicense := Some(
      HeaderLicense.MIT("2025", "the original author or authors", HeaderLicenseStyle.SpdxSyntax)
    ),
    sbtPluginPublishLegacyMavenStyle := false,
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
    scalacOptions := {
      val shared = Opts.compile.encoding("UTF-8") :+
        Opts.compile.deprecation :+
        Opts.compile.unchecked :+
        "-feature"

      // `-Ywarn-unused` is a Scala 2 spelling that Scala 3 ignores; use its Scala 3 equivalent so
      // unused-symbol linting applies on both cross-versions.
      shared ++ (scalaBinaryVersion.value match {
        case "2.12" => Seq("-Ywarn-unused")
        case "3" => Seq("-Wunused:all")
      })
    },
    libraryDependencies ++= Seq(
      dependencyCheck,
      munit           % Test,
      munitScalaCheck % Test,
      log4jSf4jImpl   % Test,
      mockito         % Test
    ),
    addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.2.0"),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        // set minimum sbt version so we have `sbtPluginPublishLegacyMavenStyle`
        // and to be able to use glob expressions on scripted tests due to
        // SBT 1.x and 2.x target files being on different paths
        case "2.12" => "1.10.7"
        case "3" => "2.0.0"
      }
    },
    javacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" => Seq("-source", "11", "-target", "11")
        case "3" => Seq("-source", "17", "-target", "17")
      }
    },
    crossScalaVersions += "3.8.4",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false,
    Test / testOptions += Tests.Argument("-F"), // Show full stack trace
    // Coverage gate: fail the build when coverage regresses below these floors. They are set just
    // below the current numbers (statement 82.93%, branch 69.53%) to leave a little headroom; raise
    // them over time as coverage improves. `testCoverage` runs `coverageReport`, which enforces this.
    coverageMinimumStmtTotal := 80.0,
    coverageMinimumBranchTotal := 65.0,
    coverageFailOnMinimum := true
  )
