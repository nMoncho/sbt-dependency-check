/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Keys.dependencyCheckScanSet
import sbt._

object ScanSet {

  def apply(): Def.Initialize[Task[Seq[File]]] = Def.task {
    collect(dependencyCheckScanSet.value)
  }

  /** Expands the configured scan-set directories into the set of files to scan.
    *
    * A user can legitimately set `dependencyCheckScanSet := Seq.empty` to scan nothing extra;
    * `reduceLeft` on an empty sequence would throw `UnsupportedOperationException`, so the empty case
    * is short-circuited to an empty result (a no-op scan set) instead of crashing the task.
    */
  private[tasks] def collect(scanSet: Seq[File]): Seq[File] =
    if (scanSet.isEmpty) Seq.empty
    else scanSet.map(_ ** "*").reduceLeft(_ +++ _).filter(_.isFile).get()

}
