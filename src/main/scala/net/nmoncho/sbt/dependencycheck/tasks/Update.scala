/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package tasks

import scala.util.control.NonFatal

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin.engineSettings
import org.owasp.dependencycheck.Engine
import sbt.Def
import sbt.Keys.streams
import sbt.Logger
import sbt.Task

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
      case t: Throwable if NonFatal(t) =>
        log.error("An exception occurred connecting to the local database:")
        logFailure(t)
        throw t
    }

}
