/*
 * Copyright (c) 2025 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._

case class ProxySettings(
    disableSchemas: Option[Boolean],
    nonProxyHosts: Option[Seq[String]]
) {

  def apply(settings: Settings): Unit = {
    settings.set(PROXY_DISABLE_SCHEMAS, disableSchemas)
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
  val Default: ProxySettings = new ProxySettings(None, None)

  def apply(
      disableSchemas: Option[Boolean]    = None,
      nonProxyHosts: Option[Seq[String]] = None
  ): ProxySettings =
    new ProxySettings(disableSchemas, nonProxyHosts)
}
