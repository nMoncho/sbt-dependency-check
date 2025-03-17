package net.nmoncho.sbt.dependencycheck
package settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*

import java.net.URL

case class HostedSuppressionsSettings(
    enabled: Option[Boolean],
    url: Option[URL],
    forceUpdate: Option[Boolean],
    validForHours: Option[Int],
    username: Option[String],
    password: Option[String],
    bearerToken: Option[String]
) {
  def apply(settings: Settings): Unit = {
    settings.set(HOSTED_SUPPRESSIONS_ENABLED, enabled)
    settings.set(HOSTED_SUPPRESSIONS_URL, url.map(_.toString))
    settings.set(HOSTED_SUPPRESSIONS_FORCEUPDATE, forceUpdate)
    settings.set(HOSTED_SUPPRESSIONS_VALID_FOR_HOURS, validForHours)

    settings.set(HOSTED_SUPPRESSIONS_USER, username)
    settings.set(HOSTED_SUPPRESSIONS_PASSWORD, password)
    settings.set(HOSTED_SUPPRESSIONS_BEARER_TOKEN, bearerToken)
  }
}

object HostedSuppressionsSettings {
  val Default: HostedSuppressionsSettings =
    new HostedSuppressionsSettings(None, None, None, None, None, None, None)

  def apply(
      enabled: Option[Boolean]     = None,
      url: Option[URL]             = None,
      forceUpdate: Option[Boolean] = None,
      validForHours: Option[Int]   = None,
      username: Option[String]     = None,
      password: Option[String]     = None,
      bearerToken: Option[String]  = None
  ): HostedSuppressionsSettings =
    new HostedSuppressionsSettings(
      enabled,
      url,
      forceUpdate,
      validForHours,
      username,
      password,
      bearerToken
    )
}
