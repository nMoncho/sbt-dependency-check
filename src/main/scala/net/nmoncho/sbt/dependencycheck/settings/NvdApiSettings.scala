package net.nmoncho.sbt.dependencycheck
package settings

import net.nmoncho.sbt.dependencycheck.settings.NvdApiSettings.DataFeed
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS.*
import sbt.URL

case class NvdApiSettings(
    endpoint: Option[String],
    apiKey: String,
    requestDelay: Option[Int],
    maxRetryCount: Option[Int],
    validForHours: Option[Int],
    resultsPerPage: Option[Int],
    dataFeed: DataFeed
) {

  def apply(settings: Settings): Unit = {
    settings.set(NVD_API_ENDPOINT, endpoint)
    settings.setStringIfNotEmpty(NVD_API_KEY, apiKey)
    settings.set(NVD_API_DELAY, requestDelay)
    settings.set(NVD_API_MAX_RETRY_COUNT, maxRetryCount)
    settings.set(NVD_API_VALID_FOR_HOURS, validForHours)
    dataFeed(settings)
  }

}

object NvdApiSettings {
  val Default: NvdApiSettings =
    new NvdApiSettings(None, "", None, None, None, None, DataFeed.Default)

  def apply(
      endpoint: Option[String]    = None,
      apiKey: String              = "",
      requestDelay: Option[Int]   = None,
      maxRetryCount: Option[Int]  = None,
      validForHours: Option[Int]  = None,
      resultsPerPage: Option[Int] = None,
      dataFeed: DataFeed          = DataFeed.Default
  ): NvdApiSettings =
    new NvdApiSettings(
      endpoint,
      apiKey,
      requestDelay,
      maxRetryCount,
      validForHours,
      resultsPerPage,
      dataFeed
    )

  case class DataFeed(
      url: Option[URL],
      startYear: Option[Int],
      validForDays: Option[Int],
      username: Option[String],
      password: Option[String],
      bearerToken: Option[String]
  ) {
    def apply(settings: Settings): Unit = {
      settings.set(NVD_API_DATAFEED_URL, url)
      settings.set(NVD_API_DATAFEED_START_YEAR, startYear)
      settings.set(NVD_API_DATAFEED_VALID_FOR_DAYS, validForDays)
      settings.set(NVD_API_DATAFEED_USER, username)
      settings.set(NVD_API_DATAFEED_PASSWORD, password)
      settings.set(NVD_API_DATAFEED_BEARER_TOKEN, bearerToken)
    }
  }

  object DataFeed {
    val Default: DataFeed = new DataFeed(None, None, None, None, None, None)

    def apply(
        url: Option[URL]            = None,
        startYear: Option[Int]      = None,
        validForDays: Option[Int]   = None,
        username: Option[String]    = None,
        password: Option[String]    = None,
        bearerToken: Option[String] = None
    ): DataFeed = new DataFeed(url, startYear, validForDays, username, password, bearerToken)
  }
}
