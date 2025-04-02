import net.nmoncho.sbt.dependencycheck.settings.*

version := "0.1"
lazy val root = project in file(".")
scalaVersion := "2.13.16"

dependencyCheckSuppressions := SuppressionSettings(
  suppressions = Seq(
    SuppressionRule(cvssBelow = Seq(8.0))
  )
)
