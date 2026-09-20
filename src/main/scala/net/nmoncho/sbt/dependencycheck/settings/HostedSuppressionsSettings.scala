/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck
package settings

import java.net.URL

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Hosted Suppressions Settings
  *
  * @param enabled whether the hosted suppressions file datasource is enabled
  * @param url hosted suppressions file URL
  * @param forceUpdate whether the hosted suppressions file will be updated regardless of the autoupdate settings.
  * @param validForHours controls the skipping of the check for hosted suppressions file updates.
  * @param username the hosted suppressions username. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Basic-authentication
  * @param password the hosted suppressions password. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Basic-authentication
  * @param bearerToken the hosted suppressions bearer token. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Bearer-authentication
  */
case class HostedSuppressionsSettings(
    enabled: Option[Boolean]     = None,
    url: Option[URL]             = None,
    forceUpdate: Option[Boolean] = None,
    validForHours: Option[Int]   = None,
    username: Option[String]     = None,
    @redacted
    password: Option[String] = None,
    @redacted
    bearerToken: Option[String] = None
) {
  def configure(settings: Settings): Unit = {
    settings.set(HOSTED_SUPPRESSIONS_ENABLED, enabled)
    settings.set(HOSTED_SUPPRESSIONS_URL, url.map(_.toString))
    settings.set(HOSTED_SUPPRESSIONS_FORCEUPDATE, forceUpdate)
    settings.set(HOSTED_SUPPRESSIONS_VALID_FOR_HOURS, validForHours)

    settings.set(HOSTED_SUPPRESSIONS_USER, username)
    settings.set(HOSTED_SUPPRESSIONS_PASSWORD, password)
    settings.set(HOSTED_SUPPRESSIONS_BEARER_TOKEN, bearerToken)
  }

  override def toString: String = redactedToString(this)
}

object HostedSuppressionsSettings {
  val Default: HostedSuppressionsSettings = HostedSuppressionsSettings()
}
