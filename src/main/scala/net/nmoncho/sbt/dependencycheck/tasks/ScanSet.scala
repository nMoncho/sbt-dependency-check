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
    dependencyCheckScanSet.value.map(_ ** "*").reduceLeft(_ +++ _).filter(_.isFile).get()
  }

}
