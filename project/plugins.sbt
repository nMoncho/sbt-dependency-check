addSbtPlugin("de.heikoseeberger" % "sbt-header"     % "5.10.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"   % "2.5.4")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"  % "2.3.1")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"    % "0.6.4")
addSbtPlugin("com.github.sbt"    % "sbt-ci-release" % "1.9.2")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"   % "0.14.2")

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
)
