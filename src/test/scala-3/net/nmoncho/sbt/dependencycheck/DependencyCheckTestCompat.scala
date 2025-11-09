/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import sbt.Classpaths
import sbt.ModuleID
import sbt.internal.util.StringAttributeKey

object DependencyCheckTestCompat {
  def attributeMap(module: ModuleID): Map[StringAttributeKey, String] =
    Map(
      sbt.Keys.moduleIDStr -> Classpaths.moduleIdJsonKeyFormat.write(module)
    )
}
