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
import sbt.Keys.streams
import sbt._

object Update {

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    withEngine(engineSettings.value) { engine =>
      Update(engine)
    }
    // Tagged NonParallel so it is serialized with every other `withEngine` caller under the exclusive
    // NonParallel restriction; this prevents two untagged engine tasks (e.g. `all dependencyCheckUpdate`
    // across a multi-project build, or Update running alongside Purge) from concurrently reconfiguring
    // the process-global `Downloader` singleton and racing on the shared NVD data directory.
  } tag NonParallel

  def apply(engine: Engine)(implicit log: Logger): Unit =
    try {
      engine.doUpdates()
    } catch {
      case NonFatal(t) =>
        // NVD data-update failures are usually remote (NVD API throttling/HTTP 403/429, connectivity,
        // or a missing API key), not a local database problem. Surface the actionable hint here; the
        // stack trace is logged once by `withEngine`, which re-catches the rethrown exception.
        log.error(
          "Failed to update the NVD data. This is often caused by NVD API rate limiting or " +
            "connectivity. Set an NVD API key via `dependencyCheckNvdApi` (see " +
            "https://nvd.nist.gov/developers/request-an-api-key), and consider increasing its " +
            "`requestDelay` or `maxRetryCount`."
        )
        throw t
    }

}
