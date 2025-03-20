import sbt.*

object Dependencies {
  lazy val dependencyCheck = "org.owasp" % "dependency-check-core" % "12.1.0"
  lazy val munit = "org.scalameta" %% "munit" % "1.0.2"
}
