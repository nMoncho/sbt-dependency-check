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

package net.nmoncho.sbt.dependencycheck
package settings

import java.time.Duration

import net.nmoncho.sbt.dependencycheck.settings.NvdApiSettings.DataFeed
import org.owasp.dependencycheck.utils.Settings
import org.owasp.dependencycheck.utils.Settings.KEYS._
import sbt.URL

/** NVD API Settings
  *
  * @param apiKey API Key for the NVD API
  * @param endpoint NVD API Endpoint
  * @param requestDelay delay between requests for the NVD API
  * @param maxRetryCount the maximum number of retry requests for a single call to the NVD API
  * @param validForHours control the skipping of the check for NVD updates
  * @param resultsPerPage control the results per page lower than NVD's default of 2000.
  * @param dataFeed NVD Data Feed Configuration
  */
case class NvdApiSettings(
    apiKey: String,
    endpoint: Option[String],
    requestDelay: Option[Duration],
    maxRetryCount: Option[Int],
    validForHours: Option[Int],
    resultsPerPage: Option[Int],
    dataFeed: DataFeed
) {

  def apply(settings: Settings): Unit = {
    settings.setStringIfNotEmpty(NVD_API_KEY, apiKey)
    settings.set(NVD_API_ENDPOINT, endpoint)
    settings.set(NVD_API_DELAY, requestDelay.map(_.toMillis))
    settings.set(NVD_API_MAX_RETRY_COUNT, maxRetryCount)
    settings.set(NVD_API_VALID_FOR_HOURS, validForHours)
    settings.set(NVD_API_RESULTS_PER_PAGE, resultsPerPage)
    dataFeed(settings)
  }

}

object NvdApiSettings {
  val Default: NvdApiSettings =
    new NvdApiSettings("", None, None, None, None, None, DataFeed.Default)

  def apply(
      apiKey: String                 = "",
      endpoint: Option[String]       = None,
      requestDelay: Option[Duration] = None,
      maxRetryCount: Option[Int]     = None,
      validForHours: Option[Int]     = None,
      resultsPerPage: Option[Int]    = None,
      dataFeed: DataFeed             = DataFeed.Default
  ): NvdApiSettings =
    new NvdApiSettings(
      apiKey,
      endpoint,
      requestDelay,
      maxRetryCount,
      validForHours,
      resultsPerPage,
      dataFeed
    )

  /** Data Feed Settings
    *
    * @param url URL for the NVD API Data Feed
    * @param startYear starting year for the NVD CVE Data feed cache.
    * @param validForDays indicates how often the NVD API data feed needs to be updated before a full refresh is evaluated
    * @param username username to use when connecting to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Basic-authentication.
    * @param password password to authenticate to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Basic-authentication.
    * @param bearerToken token to authenticate to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Bearer-authentication.
    */
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
