addSbtPlugin("com.github.sbt"   % "sbt-header"     % "5.11.0")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"   % "2.6.2")
addSbtPlugin("org.scoverage"    % "sbt-scoverage"  % "2.4.4")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"    % "0.7.0")
addSbtPlugin("com.github.sbt"   % "sbt-ci-release" % "1.12.1")
addSbtPlugin("ch.epfl.scala"    % "sbt-scalafix"   % "0.14.8")

libraryDependencies ++= Seq(
  "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
)
