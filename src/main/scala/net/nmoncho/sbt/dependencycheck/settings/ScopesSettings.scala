package net.nmoncho.sbt.dependencycheck.settings

case class ScopesSettings(
    compile: Boolean,
    test: Boolean,
    runtime: Boolean,
    provided: Boolean,
    optional: Boolean
) {

  def toPrettyString(): String =
    s"""ScopesSettings:
       |  compile: $compile
       |  test: $test
       |  runtime: $runtime
       |  provided: $provided
       |  optional: $optional
       |""".stripMargin

}

object ScopesSettings {

  final val Default: ScopesSettings = new ScopesSettings(
    compile  = true,
    test     = false,
    runtime  = true,
    provided = true,
    optional = true
  )

  def apply(
      compile: Boolean  = true,
      test: Boolean     = false,
      runtime: Boolean  = true,
      provided: Boolean = true,
      optional: Boolean = true
  ): ScopesSettings =
    new ScopesSettings(compile, test, runtime, provided, optional)
}
