package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import org.owasp.dependencycheck.Engine
import sbt.Keys.streams
import sbt.{ Def, Logger, Task }

import scala.util.control.NonFatal

object Update {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    withEngine(engineSettings.value) { engine =>
      Update(engine)
    }
  }

  def apply(engine: Engine)(implicit log: Logger): Unit =
    try {
      engine.doUpdates()
    } catch {
      case e: Exception if NonFatal(e) =>
        log.error(
          s"An exception occurred connecting to the local database: ${e.getLocalizedMessage}"
        )
        throw e
    }

}
