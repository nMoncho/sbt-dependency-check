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
import sbt._

/** File Type Analyzer Settings
  *
  * OWASP dependency-check contains several file type analyzers that are used to extract identification information from the files analyzed.
  *
  * For more information, see <a href="https://jeremylong.github.io/DependencyCheck/analyzers/index.html">File Type Analyzers</a>
  *
  * @param archiveEnabled whether the Archive analyzer is enabled.
  * @param artifactory Artifactory Settings.
  * @param autoconfEnabled whether the autoconf analyzer should be used.
  * @param cmakeEnabled whether the CMake analyzer is enabled.
  * @param cpanFileEnabled whether the Perl CPAN File analyzer is enabled.
  * @param cpeEnabled whether the CPE analyzer is enabled.
  * @param cpeSuppressionEnabled whether the CPE Suppression analyzer is enabled.
  * @param dartEnabled whether the Dart analyzer is enabled.
  * @param dependencyBundlingEnabled whether the Dependency Bundling analyzer is enabled.
  * @param dependencyMergingEnabled whether the Dependency Merging analyzer is enabled.
  * @param dotNet .NET Settings.
  * @param elixir Elixir Settings
  * @param experimentalEnabled whether experimental analyzers are enabled.
  * @param failOnUnusedSuppressionRule whether the Unused Suppression Rule analyzer should fail if there are unused rules.
  * @param falsePositiveEnabled whether the False Positive analyzer is enabled.
  * @param filenameEnabled whether the Filename analyzer is enabled.
  * @param fileVersionEnabled whether the File Version analyzer is enabled.
  * @param golang Golang Settings.
  * @param hints Hints Settings.
  * @param jarEnabled whether the JAR analyzer is enabled.
  * @param knownExploitedEnabled whether the Known Exploited Vulnerabilities analyzer is enabled.
  * @param mavenCentral Maven Central Settings
  * @param mavenInstallEnabled whether the Maven Install analyzer is enabled.
  * @param nexus Nexus Settings.
  * @param node Node Settings
  * @param nvdCveEnabled whether the NVD CVE analyzer is enabled.
  * @param openSslEnabled whether the OpenSSL analyzer is enabled.
  * @param ossIndex OSS Index Settings.
  * @param php PHP Settings.
  * @param pnmp PNPM Settings.
  * @param python Python Settings.
  * @param retiredEnabled whether the retired analyzers are enabled.
  * @param retireJS RetireJS Settings.
  * @param ruby Ruby Settings.
  * @param swift Swift Settings.
  * @param versionFilterEnabled whether the Version Filter analyzer is enabled.
  * @param vulnerabilitySuppressionEnabled whether the Vulnerability Suppression analyzer is enabled.
  * @param yarn Yarn Settings.
  */
case class AnalyzerSettings(
    archiveEnabled: Option[Boolean],
    artifactory: AnalyzerSettings.Artifactory,
    autoconfEnabled: Option[Boolean],
    cmakeEnabled: Option[Boolean],
    cpanFileEnabled: Option[Boolean],
    cpeEnabled: Option[Boolean],
    cpeSuppressionEnabled: Option[Boolean],
    dartEnabled: Option[Boolean],
    dependencyBundlingEnabled: Option[Boolean],
    dependencyMergingEnabled: Option[Boolean],
    dotNet: AnalyzerSettings.DotNet,
    elixir: AnalyzerSettings.Elixir,
    experimentalEnabled: Option[Boolean],
    failOnUnusedSuppressionRule: Option[Boolean],
    falsePositiveEnabled: Option[Boolean],
    filenameEnabled: Option[Boolean],
    fileVersionEnabled: Option[Boolean],
    golang: AnalyzerSettings.Golang,
    hints: AnalyzerSettings.Hints,
    jarEnabled: Option[Boolean],
    knownExploitedEnabled: Option[Boolean],
    mavenCentral: AnalyzerSettings.MavenCentral,
    mavenInstallEnabled: Option[Boolean],
    nexus: AnalyzerSettings.Nexus,
    node: AnalyzerSettings.Node,
    nvdCveEnabled: Option[Boolean],
    openSslEnabled: Option[Boolean],
    ossIndex: AnalyzerSettings.OssIndex,
    php: AnalyzerSettings.Php,
    pnmp: AnalyzerSettings.Pnpm,
    python: AnalyzerSettings.Python,
    retiredEnabled: Option[Boolean],
    retireJS: AnalyzerSettings.RetireJS,
    ruby: AnalyzerSettings.Ruby,
    swift: AnalyzerSettings.Swift,
    versionFilterEnabled: Option[Boolean],
    vulnerabilitySuppressionEnabled: Option[Boolean],
    yarn: AnalyzerSettings.Yarn
) {

  def apply(settings: Settings): Unit = {
    settings.set(ANALYZER_ARCHIVE_ENABLED, archiveEnabled)
    settings.set(ANALYZER_AUTOCONF_ENABLED, autoconfEnabled)
    settings.set(ANALYZER_CMAKE_ENABLED, cmakeEnabled)
    settings.set(ANALYZER_CPANFILE_ENABLED, cpanFileEnabled)
    settings.set(ANALYZER_CPE_ENABLED, cpeEnabled)
    settings.set(ANALYZER_CPE_SUPPRESSION_ENABLED, cpeSuppressionEnabled)
    settings.set(ANALYZER_DART_ENABLED, dartEnabled)
    settings.set(ANALYZER_DART_ENABLED, dartEnabled)
    settings.set(ANALYZER_DEPENDENCY_BUNDLING_ENABLED, dependencyBundlingEnabled)
    settings.set(ANALYZER_DEPENDENCY_MERGING_ENABLED, dependencyMergingEnabled)
    settings.set(ANALYZER_EXPERIMENTAL_ENABLED, experimentalEnabled)
    settings.set(FAIL_ON_UNUSED_SUPPRESSION_RULE, failOnUnusedSuppressionRule)
    settings.set(ANALYZER_FALSE_POSITIVE_ENABLED, falsePositiveEnabled)
    settings.set(ANALYZER_FILE_NAME_ENABLED, filenameEnabled)
    settings.set(ANALYZER_PE_ENABLED, fileVersionEnabled)
    settings.set(ANALYZER_JAR_ENABLED, jarEnabled)
    settings.set(ANALYZER_KNOWN_EXPLOITED_ENABLED, knownExploitedEnabled)
    settings.set(ANALYZER_MAVEN_INSTALL_ENABLED, mavenInstallEnabled)
    settings.set(ANALYZER_NVD_CVE_ENABLED, nvdCveEnabled)
    settings.set(ANALYZER_OPENSSL_ENABLED, openSslEnabled)
    settings.set(ANALYZER_RETIRED_ENABLED, retiredEnabled)
    settings.set(ANALYZER_VERSION_FILTER_ENABLED, versionFilterEnabled)
    settings.set(ANALYZER_VULNERABILITY_SUPPRESSION_ENABLED, vulnerabilitySuppressionEnabled)

    artifactory(settings)
    dotNet(settings)
    elixir(settings)
    golang(settings)
    hints(settings)
    mavenCentral(settings)
    nexus(settings)
    node(settings)
    ossIndex(settings)
    php(settings)
    pnmp(settings)
    python(settings)
    retireJS(settings)
    ruby(settings)
    swift(settings)
    yarn(settings)
  }
}

object AnalyzerSettings {

  val Default: AnalyzerSettings = new AnalyzerSettings(
    archiveEnabled                  = None,
    artifactory                     = Artifactory.Default,
    autoconfEnabled                 = None,
    cmakeEnabled                    = None,
    cpanFileEnabled                 = None,
    cpeEnabled                      = None,
    cpeSuppressionEnabled           = None,
    dartEnabled                     = None,
    dependencyBundlingEnabled       = None,
    dependencyMergingEnabled        = None,
    dotNet                          = DotNet.Default,
    elixir                          = Elixir.Default,
    experimentalEnabled             = None,
    failOnUnusedSuppressionRule     = None,
    falsePositiveEnabled            = None,
    filenameEnabled                 = None,
    fileVersionEnabled              = None,
    golang                          = Golang.Default,
    hints                           = Hints.Default,
    jarEnabled                      = None,
    knownExploitedEnabled           = None,
    mavenCentral                    = MavenCentral.Default,
    mavenInstallEnabled             = None,
    nexus                           = Nexus.Default,
    node                            = Node.Default,
    nvdCveEnabled                   = None,
    openSslEnabled                  = None,
    ossIndex                        = OssIndex.Default,
    php                             = Php.Default,
    pnmp                            = Pnpm.Default,
    python                          = Python.Default,
    retiredEnabled                  = None,
    retireJS                        = RetireJS.Default,
    ruby                            = Ruby.Default,
    swift                           = Swift.Default,
    versionFilterEnabled            = None,
    vulnerabilitySuppressionEnabled = None,
    yarn                            = Yarn.Default
  )

  def apply(
      archiveEnabled: Option[Boolean]                  = None,
      artifactory: Artifactory                         = Artifactory.Default,
      autoconfEnabled: Option[Boolean]                 = None,
      cmakeEnabled: Option[Boolean]                    = None,
      cpanFileEnabled: Option[Boolean]                 = None,
      cpeEnabled: Option[Boolean]                      = None,
      cpeSuppressionEnabled: Option[Boolean]           = None,
      dartEnabled: Option[Boolean]                     = None,
      dependencyBundlingEnabled: Option[Boolean]       = None,
      dependencyMergingEnabled: Option[Boolean]        = None,
      dotNet: DotNet                                   = DotNet.Default,
      elixir: Elixir                                   = Elixir.Default,
      experimentalEnabler: Option[Boolean]             = None,
      failOnUnusedSuppressionRule: Option[Boolean]     = None,
      falsePositiveEnabled: Option[Boolean]            = None,
      filenameEnabled: Option[Boolean]                 = None,
      fileVersionEnabled: Option[Boolean]              = None,
      golang: Golang                                   = Golang.Default,
      hints: Hints                                     = Hints.Default,
      jarEnabled: Option[Boolean]                      = None,
      knownExploitedEnabled: Option[Boolean]           = None,
      mavenCentral: MavenCentral                       = MavenCentral.Default,
      mavenInstallEnabled: Option[Boolean]             = None,
      nexus: Nexus                                     = Nexus.Default,
      node: Node                                       = Node.Default,
      nvdCveEnabled: Option[Boolean]                   = None,
      openSslEnabled: Option[Boolean]                  = None,
      ossIndex: OssIndex                               = OssIndex.Default,
      php: Php                                         = Php.Default,
      pnmp: Pnpm                                       = Pnpm.Default,
      python: Python                                   = Python.Default,
      retiredEnabled: Option[Boolean]                  = None,
      retireJS: RetireJS                               = RetireJS.Default,
      ruby: Ruby                                       = Ruby.Default,
      swift: Swift                                     = Swift.Default,
      versionFilterEnabled: Option[Boolean]            = None,
      vulnerabilitySuppressionEnabled: Option[Boolean] = None,
      yarn: Yarn                                       = Yarn.Default
  ): AnalyzerSettings = AnalyzerSettings(
    archiveEnabled,
    artifactory,
    autoconfEnabled,
    cmakeEnabled,
    cpanFileEnabled,
    cpeEnabled,
    cpeSuppressionEnabled,
    dartEnabled,
    dependencyBundlingEnabled,
    dependencyMergingEnabled,
    dotNet,
    elixir,
    experimentalEnabler,
    failOnUnusedSuppressionRule,
    falsePositiveEnabled,
    filenameEnabled,
    fileVersionEnabled,
    golang,
    hints,
    jarEnabled,
    knownExploitedEnabled,
    mavenCentral,
    mavenInstallEnabled,
    nexus,
    node,
    nvdCveEnabled,
    openSslEnabled,
    ossIndex,
    php,
    pnmp,
    python,
    retiredEnabled,
    retireJS,
    ruby,
    swift,
    versionFilterEnabled,
    vulnerabilitySuppressionEnabled,
    yarn
  )

  /** Artifactory Settings
    *
    * @param enabled whether Artifactory is enabled
    * @param url Artifactory search URL
    * @param parallelAnalysis whether should use parallel processing
    * @param apiToken Artifactory API token
    * @param username Artifactory username
    * @param bearerToken Artifactory bearer token
    * @param usesProxy whether the proxy should be used to reach Artifactory
    */
  case class Artifactory(
      enabled: Option[Boolean],
      url: Option[URL],
      parallelAnalysis: Option[Boolean],
      apiToken: Option[String],
      username: Option[String],
      bearerToken: Option[String],
      usesProxy: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_ARTIFACTORY_ENABLED, enabled)
      settings.set(ANALYZER_ARTIFACTORY_URL, url)
      settings.set(ANALYZER_ARTIFACTORY_PARALLEL_ANALYSIS, parallelAnalysis)
      settings.set(ANALYZER_ARTIFACTORY_API_TOKEN, apiToken)
      settings.set(ANALYZER_ARTIFACTORY_API_USERNAME, username)
      settings.set(ANALYZER_ARTIFACTORY_BEARER_TOKEN, bearerToken)
      settings.set(ANALYZER_ARTIFACTORY_USES_PROXY, usesProxy)
    }
  }

  object Artifactory {
    val Default: Artifactory = new Artifactory(None, None, None, None, None, None, None)

    def apply(
        enabled: Option[Boolean]          = None,
        url: Option[URL]                  = None,
        parallelAnalysis: Option[Boolean] = None,
        apiToken: Option[String]          = None,
        username: Option[String]          = None,
        bearerToken: Option[String]       = None,
        usesProxy: Option[Boolean]        = None
    ): Artifactory =
      new Artifactory(enabled, url, parallelAnalysis, apiToken, username, bearerToken, usesProxy)
  }

  /** .NET Settings
    *
    * @param assemblyEnabled whether the .NET Assembly analyzer is enabled
    * @param assemblyPath The path to dotnet core, if available
    * @param nuspecEnabled whether the .NET Nuspec analyzer is enabled
    * @param nugetConfEnabled whether the .NET Nuget packages. config analyzer is enabled
    * @param libManEnabled whether the Libman analyzer is enabled
    * @param msBuildProjectEnabled whether the .NET MSBuild Project analyzer is enabled
    */
  case class DotNet(
      assemblyEnabled: Option[Boolean],
      assemblyPath: Option[File],
      nuspecEnabled: Option[Boolean],
      nugetConfEnabled: Option[Boolean],
      libManEnabled: Option[Boolean],
      msBuildProjectEnabled: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_ASSEMBLY_ENABLED, assemblyEnabled)
      settings.set(ANALYZER_ASSEMBLY_DOTNET_PATH, assemblyPath)
      settings.set(ANALYZER_NUSPEC_ENABLED, nuspecEnabled)
      settings.set(ANALYZER_NUGETCONF_ENABLED, nugetConfEnabled)
      settings.set(ANALYZER_LIBMAN_ENABLED, libManEnabled)
      settings.set(ANALYZER_MSBUILD_PROJECT_ENABLED, msBuildProjectEnabled)
    }
  }

  object DotNet {
    val Default: DotNet = new DotNet(None, None, None, None, None, None)

    def apply(
        assemblyEnabled: Option[Boolean]       = None,
        assemblyPath: Option[File]             = None,
        nuspecEnabled: Option[Boolean]         = None,
        nugetConfEnabled: Option[Boolean]      = None,
        libManEnabled: Option[Boolean]         = None,
        msBuildProjectEnabled: Option[Boolean] = None
    ): DotNet =
      new DotNet(
        assemblyEnabled,
        assemblyPath,
        nuspecEnabled,
        nugetConfEnabled,
        libManEnabled,
        msBuildProjectEnabled
      )
  }

  /** Elixir Settings
    *
    * @param mixAuditEnabled whether the Elixir mix audit analyzer is enabled
    * @param mixAuditPath The path to mix_audit, if available
    */
  case class Elixir(
      mixAuditEnabled: Option[Boolean],
      mixAuditPath: Option[File]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_MIX_AUDIT_ENABLED, mixAuditEnabled)
      settings.set(ANALYZER_MIX_AUDIT_PATH, mixAuditPath)
    }
  }

  object Elixir {
    val Default: Elixir = new Elixir(None, None)

    def apply(mixAuditEnabled: Option[Boolean] = None, mixAuditPath: Option[File] = None): Elixir =
      new Elixir(mixAuditEnabled, mixAuditPath)
  }

  /** Golang Settings
    *
    * @param dependencyAnalyzerEnabled whether the Golang Dep analyzer is enabled
    * @param moduleAnalyzerEnabled whether the Golang Mod analyzer is enabled
    * @param path The path to go, if available
    */
  case class Golang(
      dependencyAnalyzerEnabled: Option[Boolean],
      moduleAnalyzerEnabled: Option[Boolean],
      path: Option[File]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_GOLANG_DEP_ENABLED, moduleAnalyzerEnabled)
      settings.set(ANALYZER_GOLANG_MOD_ENABLED, moduleAnalyzerEnabled)
      settings.set(ANALYZER_GOLANG_PATH, path)
    }
  }

  object Golang {
    val Default: Golang = new Golang(None, None, None)

    def apply(
        dependencyAnalyzerEnabled: Option[Boolean] = None,
        moduleAnalyzerEnabled: Option[Boolean]     = None,
        path: Option[File]                         = None
    ): Golang =
      new Golang(dependencyAnalyzerEnabled, moduleAnalyzerEnabled, path)
  }

  /** Hints Settings
    *
    * @param enabled whether the Hint analyzer is enabled
    * @param hintsFile path to the hints file
    */
  case class Hints(enabled: Option[Boolean], hintsFile: Option[String]) {
    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_HINT_ENABLED, enabled)
      settings.set(HINTS_FILE, hintsFile)
    }
  }

  object Hints {
    val Default: Hints = new Hints(None, None)

    /** Enable the Hints backed by a file
      */
    def enable(file: File): Hints = new Hints(Some(true), Some(file.getAbsolutePath))

    /** Enable the Hints backed by a URL
      */
    def enable(url: URL): Hints = new Hints(Some(true), Some(url.toExternalForm))
  }

  /** Maven Central Settings
    *
    * @param enabled whether the Maven Central analyzer is enabled
    * @param url the Maven Central search URL
    * @param query the Maven Central search query
    * @param usesCache whether Maven Central search results will be cached
    * @param retryCount the Maven Central analyzer request retry count
    * @param parallelAnalysis whether the Maven Central analyzer should use parallel processing
    * @param username the Username to obtain content from Maven Central. For use when the central content URL is reconfigured to a site requiring HTTP-Basic-authentication
    * @param password the Password to obtain content from Maven Central. For use when the central content URL is reconfigured to a site requiring HTTP-Basic-authentication
    * @param bearerToken the token to obtain content from Maven Central from an HTTP-Bearer-auth protected location. For use when the central content URL is reconfigured to a site requiring HTTP-Bearer-authentication
    */
  case class MavenCentral(
      enabled: Option[Boolean],
      url: Option[URL],
      query: Option[String],
      usesCache: Option[Boolean],
      retryCount: Option[Int],
      parallelAnalysis: Option[Boolean],
      username: Option[String],
      password: Option[String],
      bearerToken: Option[String]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_CENTRAL_ENABLED, enabled)
      settings.set(ANALYZER_CENTRAL_URL, url)
      settings.set(ANALYZER_CENTRAL_QUERY, query)
      settings.set(ANALYZER_CENTRAL_USE_CACHE, usesCache)
      settings.set(ANALYZER_CENTRAL_RETRY_COUNT, retryCount)
      settings.set(ANALYZER_CENTRAL_PARALLEL_ANALYSIS, parallelAnalysis)
      settings.set(ANALYZER_CENTRAL_USER, username)
      settings.set(ANALYZER_CENTRAL_PASSWORD, password)
      settings.set(ANALYZER_CENTRAL_BEARER_TOKEN, bearerToken)
    }

  }

  object MavenCentral {
    val Default: MavenCentral =
      new MavenCentral(None, None, None, None, None, None, None, None, None)

    def apply(
        enabled: Option[Boolean]          = None,
        url: Option[URL]                  = None,
        query: Option[String]             = None,
        usesCache: Option[Boolean]        = None,
        retryCount: Option[Int]           = None,
        parallelAnalysis: Option[Boolean] = None,
        username: Option[String]          = None,
        password: Option[String]          = None,
        bearerToken: Option[String]       = None
    ): MavenCentral =
      new MavenCentral(
        enabled,
        url,
        query,
        usesCache,
        retryCount,
        parallelAnalysis,
        username,
        password,
        bearerToken
      )
  }

  /** Nexus Settings
    *
    * @param enabled whether the Nexus analyzer is enabled
    * @param url the Nexus search URL
    * @param username the Nexus search credentials username
    * @param password the Nexus search credentials password
    * @param usesProxy whether to use the proxy to reach Nexus
    */
  case class Nexus(
      enabled: Option[Boolean],
      url: Option[URL],
      username: Option[String],
      password: Option[String],
      usesProxy: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_NEXUS_ENABLED, enabled)
      settings.set(ANALYZER_NEXUS_URL, url)
      settings.set(ANALYZER_CENTRAL_USER, username)
      settings.set(ANALYZER_CENTRAL_PASSWORD, password)
      settings.set(ANALYZER_NEXUS_USES_PROXY, usesProxy)
    }

  }

  object Nexus {
    val Default: Nexus = new Nexus(None, None, None, None, None)

    def apply(
        enabled: Option[Boolean]   = None,
        url: Option[URL]           = None,
        username: Option[String]   = None,
        password: Option[String]   = None,
        usesProxy: Option[Boolean] = None
    ): Nexus =
      new Nexus(enabled, url, username, password, usesProxy)
  }

  /** Node Settings
    *
    * @param auditEnabled whether the Node Audit analyzer is enabled
    * @param auditUrl the URL to the Node Audit API
    * @param auditSkipDevDependencies whether the Node Audit analyzer should skip devDependencies
    * @param auditUsesCache whether node audit analyzer results will be cached
    * @param packageEnabled whether the Node Package analyzer is enabled
    * @param packageSkipDevDependencies whether the Node Package analyzer should skip devDependencies
    * @param npmCpeEnabled where the NPM CPE analyzer is enabled
    */
  case class Node(
      auditEnabled: Option[Boolean],
      auditUrl: Option[URL],
      auditSkipDevDependencies: Option[Boolean],
      auditUsesCache: Option[Boolean],
      packageEnabled: Option[Boolean],
      packageSkipDevDependencies: Option[Boolean],
      npmCpeEnabled: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_NODE_AUDIT_ENABLED, auditEnabled)
      settings.set(ANALYZER_NODE_AUDIT_URL, auditUrl)
      settings.set(ANALYZER_NODE_AUDIT_SKIPDEV, auditSkipDevDependencies)
      settings.set(ANALYZER_NODE_AUDIT_USE_CACHE, auditSkipDevDependencies)

      settings.set(ANALYZER_NODE_PACKAGE_ENABLED, packageEnabled)
      settings.set(ANALYZER_NODE_PACKAGE_SKIPDEV, packageSkipDevDependencies)

      settings.set(ANALYZER_NPM_CPE_ENABLED, npmCpeEnabled)
    }

  }

  object Node {
    val Default: Node = new Node(None, None, None, None, None, None, None)

    def apply(
        auditEnabled: Option[Boolean]               = None,
        auditUrl: Option[URL]                       = None,
        auditSkipDevDependencies: Option[Boolean]   = None,
        auditUsesCache: Option[Boolean]             = None,
        packageEnabled: Option[Boolean]             = None,
        packageSkipDevDependencies: Option[Boolean] = None,
        npmCpeEnabled: Option[Boolean]              = None
    ): Node =
      new Node(
        auditEnabled,
        auditUrl,
        auditSkipDevDependencies,
        auditUsesCache,
        packageEnabled,
        packageSkipDevDependencies,
        npmCpeEnabled
      )
  }

  /** Sonatype OSS Index Settings
    *
    * @param enabled whether the Sonatype OSS Index analyzer is enabled
    * @param url the Sonatype OSS Index URL
    * @param batchSize the Sonatype OSS batch-size
    * @param requestDelay the Sonatype OSS Request Delay. Amount of time in seconds to wait before executing a request against the Sonatype OSS Rest API
    * @param useCache whether the Sonatype OSS Index should use a local cache
    * @param warnOnlyOnRemoteErrors only warning about Sonatype OSS Index remote errors instead of failing the request
    * @param username the Sonatype OSS Index user
    * @param password the Sonatype OSS Index password
    */
  case class OssIndex(
      enabled: Option[Boolean],
      url: Option[URL],
      batchSize: Option[Int],
      requestDelay: Option[Int],
      useCache: Option[Boolean],
      warnOnlyOnRemoteErrors: Option[Boolean],
      username: Option[String],
      password: Option[String]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_OSSINDEX_ENABLED, enabled)
      settings.set(ANALYZER_OSSINDEX_URL, url)
      settings.set(ANALYZER_OSSINDEX_BATCH_SIZE, batchSize)
      settings.set(ANALYZER_OSSINDEX_REQUEST_DELAY, requestDelay)
      settings.set(ANALYZER_OSSINDEX_USE_CACHE, useCache)
      settings.set(ANALYZER_OSSINDEX_WARN_ONLY_ON_REMOTE_ERRORS, warnOnlyOnRemoteErrors)
      settings.set(ANALYZER_OSSINDEX_USER, username)
      settings.set(ANALYZER_OSSINDEX_PASSWORD, password)
    }

  }

  object OssIndex {
    val Default: OssIndex = new OssIndex(None, None, None, None, None, None, None, None)

    def apply(
        enabled: Option[Boolean]                = None,
        url: Option[URL]                        = None,
        batchSize: Option[Int]                  = None,
        requestDelay: Option[Int]               = None,
        useCache: Option[Boolean]               = None,
        warnOnlyOnRemoteErrors: Option[Boolean] = None,
        username: Option[String]                = None,
        password: Option[String]                = None
    ): OssIndex =
      new OssIndex(
        enabled,
        url,
        batchSize,
        requestDelay,
        useCache,
        warnOnlyOnRemoteErrors,
        username,
        password
      )
  }

  /** PHP Settings
    *
    * @param composerLockEnabled whether the PHP composer lock file analyzer is enabled
    * @param composerLockSkipDevDependencies whether the PHP composer lock file analyzer should skip dev packages
    */
  case class Php(
      composerLockEnabled: Option[Boolean],
      composerLockSkipDevDependencies: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_COMPOSER_LOCK_ENABLED, composerLockSkipDevDependencies)
      settings.set(ANALYZER_COMPOSER_LOCK_SKIP_DEV, composerLockSkipDevDependencies)
    }
  }

  object Php {
    val Default: Php = new Php(None, None)

    def apply(
        composerLockEnabled: Option[Boolean]             = None,
        composerLockSkipDevDependencies: Option[Boolean] = None
    ): Php =
      new Php(composerLockEnabled, composerLockSkipDevDependencies)
  }

  /** pnpm Settings
    *
    * @param auditEnabled whether the pnpm Audit analyzer is enabled
    * @param path the path to pnpm if available
    */
  case class Pnpm(
      auditEnabled: Option[Boolean],
      path: Option[File]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_PNPM_AUDIT_ENABLED, auditEnabled)
      settings.set(ANALYZER_PNPM_PATH, path)
    }
  }

  object Pnpm {
    val Default: Pnpm = new Pnpm(None, None)

    def apply(auditEnabled: Option[Boolean] = None, path: Option[File] = None): Pnpm =
      new Pnpm(auditEnabled, path)
  }

  /** Python Settings
    *
    * @param pipEnabled whether the pip analyzer is enabled
    * @param pipFileEnabled whether the pipfile analyzer is enabled
    * @param distributionEnabled whether the Python Distribution analyzer is enabled
    * @param packageEnabled whether the Python Package analyzer is enabled
    * @param poetryEnabled whether the Poetry analyzer is enabled
    */
  case class Python(
      pipEnabled: Option[Boolean],
      pipFileEnabled: Option[Boolean],
      distributionEnabled: Option[Boolean],
      packageEnabled: Option[Boolean],
      poetryEnabled: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_PIP_ENABLED, pipEnabled)
      settings.set(ANALYZER_PIPFILE_ENABLED, pipEnabled)

      settings.set(ANALYZER_PYTHON_DISTRIBUTION_ENABLED, distributionEnabled)
      settings.set(ANALYZER_PYTHON_PACKAGE_ENABLED, packageEnabled)

      settings.set(ANALYZER_POETRY_ENABLED, poetryEnabled)
    }
  }

  object Python {
    val Default: Python = new Python(None, None, None, None, None)

    def apply(
        pipEnabled: Option[Boolean]          = None,
        pipFileEnabled: Option[Boolean]      = None,
        distributionEnabled: Option[Boolean] = None,
        packageEnabled: Option[Boolean]      = None,
        poetryEnabled: Option[Boolean]       = None
    ): Python =
      new Python(pipEnabled, pipFileEnabled, distributionEnabled, packageEnabled, poetryEnabled)
  }

  /** RetireJS Settings
    *
    * @param enabled hether the RetireJS analyzer is enabled
    * @param forceUpdate whether the RetireJS repository will be updated regardless of the autoupdate settings
    * @param filters whether the RetireJS analyzer file content filters
    * @param filterNonVulnerable whether the RetireJS analyzer should filter out non-vulnerable dependencies
    * @param url the URL to the RetireJS repository
    * @param username the RetireJS Repository username. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Basic-authentication
    * @param password the RetireJS Repository password. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Basic-authentication
    * @param bearerToken the token to download the RetireJS JSON data from an HTTP-Bearer-auth protected location. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Bearer-authentication.
    * @param validForHours to control the skipping of the check for CVE updates
    */
  case class RetireJS(
      enabled: Option[Boolean],
      forceUpdate: Option[Boolean],
      filters: Option[Seq[String]],
      filterNonVulnerable: Option[Boolean],
      url: Option[URL],
      username: Option[String],
      password: Option[String],
      bearerToken: Option[String],
      validForHours: Option[Int]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_RETIREJS_ENABLED, enabled)
      settings.set(ANALYZER_RETIREJS_REPO_JS_URL, url)
      settings.set(ANALYZER_RETIREJS_FORCEUPDATE, forceUpdate)
      settings.set(ANALYZER_RETIREJS_FILTERS, filters)
      settings.set(ANALYZER_RETIREJS_FILTER_NON_VULNERABLE, filterNonVulnerable)
      settings.set(ANALYZER_RETIREJS_REPO_JS_USER, username)
      settings.set(ANALYZER_RETIREJS_REPO_JS_PASSWORD, password)
      settings.set(ANALYZER_RETIREJS_REPO_JS_BEARER_TOKEN, bearerToken)
      settings.set(ANALYZER_RETIREJS_REPO_VALID_FOR_HOURS, validForHours)
    }

  }

  object RetireJS {
    val Default: RetireJS =
      new RetireJS(None, None, None, None, None, None, None, None, None)

    def apply(
        enabled: Option[Boolean]             = None,
        forceUpdate: Option[Boolean]         = None,
        filters: Option[Seq[String]]         = None,
        filterNonVulnerable: Option[Boolean] = None,
        url: Option[URL]                     = None,
        username: Option[String]             = None,
        password: Option[String]             = None,
        bearerToken: Option[String]          = None,
        validForHours: Option[Int]           = None
    ): RetireJS =
      new RetireJS(
        enabled,
        forceUpdate,
        filters,
        filterNonVulnerable,
        url,
        username,
        password,
        bearerToken,
        validForHours
      )
  }

  /** Ruby Settings
    *
    * @param gemSpecEnabled whether the Ruby Gemspec Analyzer is enabled
    * @param bundleAuditEnabled whether the Ruby Bundler Audit analyzer is enabled
    * @param bundleAuditPath The path to bundle-audit, if available
    * @param bundleAuditWorkingDirectory bundle-audit working directory
    */
  case class Ruby(
      gemSpecEnabled: Option[Boolean],
      bundleAuditEnabled: Option[Boolean],
      bundleAuditPath: Option[File],
      bundleAuditWorkingDirectory: Option[File]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_RUBY_GEMSPEC_ENABLED, gemSpecEnabled)
      settings.set(ANALYZER_BUNDLE_AUDIT_ENABLED, bundleAuditEnabled)
      settings.set(ANALYZER_BUNDLE_AUDIT_PATH, bundleAuditPath)
      settings.set(ANALYZER_BUNDLE_AUDIT_WORKING_DIRECTORY, bundleAuditWorkingDirectory)
    }
  }

  object Ruby {
    val Default: Ruby = new Ruby(None, None, None, None)

    def apply(
        gemSpecEnabled: Option[Boolean]           = None,
        bundleAuditEnabled: Option[Boolean]       = None,
        bundleAuditPath: Option[File]             = None,
        bundleAuditWorkingDirectory: Option[File] = None
    ): Ruby =
      new Ruby(gemSpecEnabled, bundleAuditEnabled, bundleAuditPath, bundleAuditWorkingDirectory)
  }

  /** Swift Settings
    *
    * @param packageManagerEnabled whether the SWIFT package manager analyzer is enabled
    * @param packageResolvedEnabled whether the SWIFT package resolved analyzer is enabled
    * @param carthageEnabled whether the carthage analyzer is enabled
    * @param cocoapodsEnabled whether the cocoapods analyzer is enabled
    */
  case class Swift(
      packageManagerEnabled: Option[Boolean],
      packageResolvedEnabled: Option[Boolean],
      carthageEnabled: Option[Boolean],
      cocoapodsEnabled: Option[Boolean]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, packageManagerEnabled)
      settings.set(ANALYZER_SWIFT_PACKAGE_RESOLVED_ENABLED, packageResolvedEnabled)
      settings.set(ANALYZER_CARTHAGE_ENABLED, carthageEnabled)
      settings.set(ANALYZER_COCOAPODS_ENABLED, cocoapodsEnabled)
    }
  }

  object Swift {
    val Default: Swift = new Swift(None, None, None, None)

    def apply(
        packageManagerEnabled: Option[Boolean]  = None,
        packageResolvedEnabled: Option[Boolean] = None,
        carthageEnabled: Option[Boolean]        = None,
        cocoapodsEnabled: Option[Boolean]       = None
    ): Swift =
      new Swift(packageManagerEnabled, packageResolvedEnabled, carthageEnabled, cocoapodsEnabled)
  }

  /** Yarn Settings
    *
    * @param auditEnabled whether the Yarn Audit analyzer is enabled
    * @param path the path to Yarn if available
    */
  case class Yarn(
      auditEnabled: Option[Boolean],
      path: Option[File]
  ) {

    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_YARN_AUDIT_ENABLED, auditEnabled)
      settings.set(ANALYZER_YARN_PATH, path)
    }
  }

  object Yarn {
    val Default: Yarn = new Yarn(None, None)

    def apply(auditEnabled: Option[Boolean] = None, path: Option[File] = None): Yarn =
      new Yarn(auditEnabled, path)
  }
}
