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
        log.info(s"\t$key: ${redactUserInfo(value)}")
      }
    }
    log.info("\n\n")
  }

  // Matches the `user[:password]@` userinfo of a URL/JDBC connection string (e.g.
  // `jdbc:mysql://user:secret@host/db`), which OWASP's key-name mask does not cover. Group 1 keeps
  // the leading `//` so the URL stays readable after the credentials are stripped.
  private val UserInfoPattern = Pattern.compile("(//)[^/@\\s:]+(?::[^/@\\s]+)?@")

  /** Redacts any embedded `user:password@` credentials from a printed value, leaving the rest of the
    * connection string / URL intact for diagnostics.
    */
  private def redactUserInfo(value: String): String =
    if (value == null) null
    else UserInfoPattern.matcher(value).replaceAll("$1****@")

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
