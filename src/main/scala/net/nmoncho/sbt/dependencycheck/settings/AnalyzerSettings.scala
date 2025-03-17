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
  * @param archiveEnabled Whether or not the Archive analyzer is enabled.
  * @param artifactory Artifactory Settings.
  * @param autoconfEnabled Whether or not the autoconf analyzer should be used.
  * @param cmakeEnabled Whether or not the CMake analyzer is enabled.
  * @param cpanFileEnabled Whether or not the Perl CPAN File analyzer is enabled.
  * @param cpeEnabled Whether or not the CPE analyzer is enabled.
  * @param cpeSuppressionEnabled Whether or not the CPE Suppression analyzer is enabled.
  * @param dartEnabled Whether or not the Dart analyzer is enabled.
  * @param dependencyBundlingEnabled Whether or not the Dependency Bundling analyzer is enabled.
  * @param dependencyMergingEnabled Whether or not the Dependency Merging analyzer is enabled.
  * @param dotNet .NET Settings.
  * @param elixir Elixir Settings
  * @param experimentalEnabled Whether or not experimental analyzers are enabled.
  * @param failOnUnusedSuppressionRule Whether the Unused Suppression Rule analyzer should fail if there are unused rules.
  * @param falsePositiveEnabled Whether or not the False Positive analyzer is enabled.
  * @param filenameEnabled Whether or not the Filename analyzer is enabled.
  * @param fileVersionEnabled Whether or not the File Version analyzer is enabled.
  * @param golang Golang Settings.
  * @param hints Hints Settings.
  * @param jarEnabled Whether or not the JAR analyzer is enabled.
  * @param knownExploitedEnabled Whether or not the Known Exploited Vulnerabilities analyzer is enabled.
  * @param mavenCentral Maven Central Settings
  * @param mavenInstallEnabled Whether or not the Maven Install analyzer is enabled.
  * @param nexus Nexus Settings.
  * @param node Node Settings
  * @param nvdCveEnabled Whether or not the NVD CVE analyzer is enabled.
  * @param openSslEnabled Whether or not the OpenSSL analyzer is enabled.
  * @param ossIndex OSS Index Settings.
  * @param php PHP Settings.
  * @param pnmp PNPM Settings.
  * @param python Python Settings.
  * @param retiredEnabled Whether or not the retired analyzers are enabled.
  * @param retireJS RetireJS Settings.
  * @param ruby Ruby Settings.
  * @param swift Swift Settings.
  * @param versionFilterEnabled Whether or not the Version Filter analyzer is enabled.
  * @param vulnerabilitySuppressionEnabled Whether or not the Vulnerability Suppression analyzer is enabled.
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

  case class Hints(enabled: Option[Boolean], hintsFile: Option[String]) {
    def apply(settings: Settings): Unit = {
      settings.set(ANALYZER_HINT_ENABLED, enabled)
      settings.set(HINTS_FILE, hintsFile)
    }
  }

  object Hints {
    val Default: Hints = new Hints(None, None)

    def enable(file: File): Hints = new Hints(Some(true), Some(file.getAbsolutePath))

    def enable(url: URL): Hints = new Hints(Some(true), Some(url.toExternalForm))
  }

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
