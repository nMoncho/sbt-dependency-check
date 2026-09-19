/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

/** Signals that the plugin was misconfigured, for example a `dependencyCheckSettingsFile` that is
  * present but cannot be opened or parsed. Distinct from a scan failure or a found vulnerability so
  * callers can tell configuration errors apart.
  */
class ConfigurationException(message: String, cause: Throwable)
    extends IllegalStateException(message, cause)
