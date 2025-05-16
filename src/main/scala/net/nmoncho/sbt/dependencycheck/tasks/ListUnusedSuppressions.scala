/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import sbt._

object ListUnusedSuppressions {

  def apply(): Def.Initialize[Task[Unit]] = Check().toTask(" list-unused-suppressions")

}
