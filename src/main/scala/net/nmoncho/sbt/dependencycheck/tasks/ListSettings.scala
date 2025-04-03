/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import java.util.regex.Pattern

import net.nmoncho.sbt.dependencycheck.DependencyCheckPlugin
import net.nmoncho.sbt.dependencycheck.Keys
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import org.owasp.dependencycheck.utils.Settings
import sbt.Keys.streams
import sbt._

object ListSettings {

  private val autoImport: Keys.type = Keys

  import autoImport.*

  def apply(): Def.Initialize[Task[Unit]] = Def.task {
    implicit val log: Logger = streams.value.log

    val settings = DependencyCheckPlugin.engineSettings.value
    val scopes   = dependencyCheckScopes.value

    ListSettings(settings, scopes)
  }

  def apply(settings: Settings, scopes: ScopesSettings)(implicit log: Logger): Unit = {
    // Rebuild Masks for Masked Properties
    val masks = Option(settings.getArray(Settings.KEYS.MASKED_PROPERTIES))
      .getOrElse(Array.empty[String])
      .map(Pattern.compile(_).asPredicate())

    log.info(scopes.toPrettyString().split('\n').mkString("\t", "\n\t", ""))

    keys().toSeq.sorted.foreach { key =>
      val value = settings.getString(key)

      if (value != null && masks.exists(_.test(key))) {
        log.info(s"\t$key: ********")
      } else {
        log.info(s"\t$key: $value")
      }
    }
  }

  /** Collect all [[Settings.KEYS]] values
    *
    * This method uses reflection due to the lack of methods to iterate over available keys
    *
    * @return All [[Settings.KEYS]] values
    */
  private def keys(): Set[String] = {
    val clazz = classOf[Settings.KEYS]

    clazz.getDeclaredFields.map { field =>
      field.get(clazz).asInstanceOf[String]
    }.toSet
  }
}
