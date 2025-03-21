import sbt.*

object Dependencies {
  lazy val dependencyCheck = "org.owasp"      % "dependency-check-core" % "12.1.0"
  lazy val munit           = "org.scalameta" %% "munit"                 % "1.1.0"
  lazy val munitScalaCheck = "org.scalameta" %% "munit-scalacheck"      % "1.1.0"
}
