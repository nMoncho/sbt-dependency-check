/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

/** Proxy Settings
  *
  * @param nonProxyHosts The properties key for the non proxy hosts.
  */
case class ProxySettings(
    nonProxyHosts: Option[Seq[String]]
) {

  def apply(settings: Settings): Unit = {
    settings.set(PROXY_NON_PROXY_HOSTS, nonProxyHosts)

    val httpsProxyHost = sys.props.get("https.proxyHost")
    val httpsProxyPort = sys.props.get("https.proxyPort")

    if (httpsProxyHost.isDefined && httpsProxyPort.isDefined) {
      settings.set(PROXY_SERVER, httpsProxyHost)
      settings.set(PROXY_PORT, httpsProxyPort.map(_.toInt))
      settings.set(PROXY_USERNAME, sys.props.get("https.proxyUser"))
      settings.set(PROXY_PASSWORD, sys.props.get("https.proxyPassword"))
    } else {
      settings.set(PROXY_SERVER, sys.props.get("http.proxyHost"))
      settings.set(PROXY_PORT, sys.props.get("http.proxyPort").map(_.toInt))
      settings.set(PROXY_USERNAME, sys.props.get("http.proxyUser"))
      settings.set(PROXY_PASSWORD, sys.props.get("http.proxyPassword"))
    }
  }

}

object ProxySettings {
  val Default: ProxySettings = new ProxySettings(None)

  def apply(nonProxyHosts: Option[Seq[String]] = None): ProxySettings =
    new ProxySettings(nonProxyHosts)
}
