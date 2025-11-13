/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import sbt.ModuleID
import sbt.internal.util.AttributeEntry
import sbt.internal.util.AttributeMap

object DependencyCheckTestCompat {
  def attributeMap(module: ModuleID): AttributeMap =
    AttributeMap(AttributeEntry(sbt.Keys.moduleID.key, module))
}
