package net.nmoncho.sbt.dependencycheck.settings

import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*

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
