import sbt.*

object Dependencies {
  lazy val dependencyCheck = "org.owasp"                % "dependency-check-core" % "12.1.3"
  lazy val munit           = "org.scalameta"           %% "munit"                 % "1.1.1"
  lazy val munitScalaCheck = "org.scalameta"           %% "munit-scalacheck"      % "1.2.0"
  lazy val log4jSf4jImpl   = "org.apache.logging.log4j" % "log4j-slf4j2-impl"     % "2.24.3"
  lazy val mockito         = "org.mockito"              % "mockito-core"          % "5.20.0"
}
