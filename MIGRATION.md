# Migration Guide

To migrate your project from Alexander v. Buchholtz's "sbt-dependency-check"` please follow these steps: 

1. Replace the plugin installation from `"net.vonbuchholtz" % "sbt-dependency-check" % "x.x.x"` to `"net.nmoncho" % "sbt-dependency-check" % "y.y.y"`
2. Update your SBT Settings Keys according to the provided [Settings Mapping](#settings-mapping)
3. (Recommended) [Requesting](https://nvd.nist.gov/developers/request-an-api-key) and set your NVD API Key on the SBT Setting Key `dependencyCheckNvdApi`.
4. Rename the usage of the SBT Task `dependencyCheckAnyProject` to `dependencyCheckAllProjects`


## Settings Mapping

| Previous Setting                      | New Setting                                                                                                |
|:--------------------------------------|:-----------------------------------------------------------------------------------------------------------|
| `dependencyCheckAutoUpdate`           | Same value                                                                                                 |
| `dependencyCheckCveValidForHours`     | Sets the number of hours to wait before checking for new updates from the NVD.                             |
| `dependencyCheckFailBuildOnCVSS`      | Same value.                                                                                                |
| `dependencyCheckJUnitFailBuildOnCVSS` | Same value.                                                                                                |
| `dependencyCheckFormat`               | Combined with `dependencyCheckFormats`.                                                                    |
| `dependencyCheckFormats`              | Same value, but accepts `org.owasp.dependencycheck.reporting.ReportGenerator.Format` instead of `String`s. |
| `dependencyCheckOutputDirectory`      | Same value                                                                                                 |
| `dependencyCheckScanSet`              | Same value                                                                                                 |
| `dependencyCheckSkip`                 | Same value                                                                                                 |
| `dependencyCheckSkipTestScope`        | Moved to `dependencyCheckScopes` using the `test` field.                                                   |
| `dependencyCheckSkipRuntimeScope`     | Moved to `dependencyCheckScopes` using the `runtime` field.                                                |
| `dependencyCheckSkipProvidedScope`    | Moved to `dependencyCheckScopes` using the `provided` field.                                               |
| `dependencyCheckSkipOptionalScope`    | Moved to `dependencyCheckScopes` using the `optional` field.                                               |
| `dependencyCheckSuppressionFiles`     | Same value, but accepts both `java.io.File`s and `java.net.URL`s.                                          |
| `dependencyCheckCpeStartsWith`        | The starting String to identify the CPEs that are qualified to be imported.                                |
| `dependencyCheckHintsFile`            | Moved to `dependencyCheckAnalyzers` under the `hints` field, for the Hints Analyzer Settings.              |
| `dependencyCheckUseSbtModuleIdAsGav`  | Removed. This is enabled by default.                                                                       |
| `dependencyCheckAnalysisTimeout`      | Same value, but accepts `java.time.Duration` instead of `Int`.                                             |
| `dependencyCheckEnableExperimental`   | Moved to `dependencyCheckAnalyzers` under the `experimentalEnabled` field.                                 |
| `dependencyCheckEnableRetired`        | Moved to `dependencyCheckAnalyzers` under the `retiredEnabled` field.                                      |


### Analyzer Configuration

| Previous Setting                                     | New Setting                                                                          |
|:-----------------------------------------------------|:-------------------------------------------------------------------------------------|
| `dependencyCheckArchiveAnalyzerEnabled`              | Moved to `dependencyCheckAnalyzers` under the `archiveEnabled` field.                |
| `dependencyCheckArtifactoryAnalyzerApiToken`         | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerBearerToken`      | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerEnabled`          | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerParallelAnalysis` | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerUrl`              | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerUseProxy`         | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckArtifactoryAnalyzerUsername`         | Moved to `dependencyCheckAnalyzers` under the `artifactory` field.                   |
| `dependencyCheckAssemblyAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckAutoconfAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `autoconfEnabled` field.               |
| `dependencyCheckBundleAuditEnabled`                  | Moved to `dependencyCheckAnalyzers` under the `ruby` field.                          |
| `dependencyCheckBundleAuditWorkingDirectory`         | Moved to `dependencyCheckAnalyzers` under the `ruby` field.                          |
| `dependencyCheckCentralAnalyzerEnabled`              | Moved to `dependencyCheckAnalyzers` under the `mavenCentral` field.                  |
| `dependencyCheckCentralAnalyzerUseCache`             | Moved to `dependencyCheckAnalyzers` under the `mavenCentral` field.                  |
| `dependencyCheckCmakeAnalyzerEnabled`                | Moved to `dependencyCheckAnalyzers` under the `cmakeEnabled` field.                  |
| `dependencyCheckCocoapodsEnabled`                    | Moved to `dependencyCheckAnalyzers` under the `swift` field.                         |
| `dependencyCheckComposerAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `php` field.                           |
| `dependencyCheckCpanFileAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `cpanFileEnabled` field.               |
| `dependencyCheckDartAnalyzerEnabled`                 | Moved to `dependencyCheckAnalyzers` under the `dartEnabled` field.                   |
| `dependencyCheckGolangDepEnabled`                    | Moved to `dependencyCheckAnalyzers` under the `golang` field.                        |
| `dependencyCheckGolangModEnabled`                    | Moved to `dependencyCheckAnalyzers` under the `golang` field.                        |
| `dependencyCheckJarAnalyzerEnabled`                  | Moved to `dependencyCheckAnalyzers` under the `jarEnabled` field.                    |
| `dependencyCheckKnownExploitedEnabled`               | Moved to `dependencyCheckAnalyzers` under the `knownExploitedVulnerabilities` field. |
| `dependencyCheckKnownExploitedUrl`                   | Moved to `dependencyCheckAnalyzers` under the `knownExploitedVulnerabilities` field. |
| `dependencyCheckKnownExploitedValidForHours`         | Moved to `dependencyCheckAnalyzers` under the `knownExploitedVulnerabilities` field. |
| `dependencyCheckMavenInstallAnalyzerEnabled`         | Moved to `dependencyCheckAnalyzers` under the `mavenInstallEnabled` field.           |
| `dependencyCheckMixAuditAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `elixir` field.                        |
| `dependencyCheckMixAuditPath`                        | Moved to `dependencyCheckAnalyzers` under the `elixir` field.                        |
| `dependencyCheckMSBuildAnalyzerEnabled`              | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckNexusAnalyzerEnabled`                | Moved to `dependencyCheckAnalyzers` under the `nexus` field.                         |
| `dependencyCheckNexusPassword`                       | Moved to `dependencyCheckAnalyzers` under the `nexus` field.                         |
| `dependencyCheckNexusUrl`                            | Moved to `dependencyCheckAnalyzers` under the `nexus` field.                         |
| `dependencyCheckNexusUser`                           | Moved to `dependencyCheckAnalyzers` under the `nexus` field.                         |
| `dependencyCheckNexusUsesProxy`                      | Moved to `dependencyCheckAnalyzers` under the `nexus` field.                         |
| `dependencyCheckNodeAnalyzerEnabled`                 | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNodeAuditAnalyzerEnabled`            | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNodeAuditAnalyzerUrl`                | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNodeAuditAnalyzerUseCache`           | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNodeAuditSkipDevDependencies`        | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNodePackageSkipDevDependencies`      | Moved to `dependencyCheckAnalyzers` under the `node` field.                          |
| `dependencyCheckNPMCPEAnalyzerEnabled`               | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckNugetConfAnalyzerEnabled`            | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckNuspecAnalyzerEnabled`               | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckOpensslAnalyzerEnabled`              | Moved to `dependencyCheckAnalyzers` under the `openSslEnabled` field.                |
| `dependencyCheckOSSIndexAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckOSSIndexAnalyzerPassword`            | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckOSSIndexAnalyzerUrl`                 | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckOSSIndexAnalyzerUseCache`            | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckOSSIndexAnalyzerUsername`            | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckOSSIndexWarnOnlyOnRemoteErrors`      | Moved to `dependencyCheckAnalyzers` under the `ossIndex` field.                      |
| `dependencyCheckPathToBundleAudit`                   | Moved to `dependencyCheckAnalyzers` under the `ruby` field.                          |
| `dependencyCheckPathToDotNETCore`                    | Moved to `dependencyCheckAnalyzers` under the `dotNet` field.                        |
| `dependencyCheckPathToGo`                            | Moved to `dependencyCheckAnalyzers` under the `golang` field.                        |
| `dependencyCheckPathToPNPM`                          | Moved to `dependencyCheckAnalyzers` under the `pnpm` field.                          |
| `dependencyCheckPathToYarn`                          | Moved to `dependencyCheckAnalyzers` under the `yarn` field.                          |
| `dependencyCheckPEAnalyzerEnabled`                   | Moved to `dependencyCheckAnalyzers` under the `fileVersionEnabled` field.            |
| `dependencyCheckPipAnalyzerEnabled`                  | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckPipfileAnalyzerEnabled`              | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckPNPMAuditAnalyzerEnabled`            | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckPoetryAnalyzerEnabled`               | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckPyDistributionAnalyzerEnabled`       | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckPyPackageAnalyzerEnabled`            | Moved to `dependencyCheckAnalyzers` under the `python` field.                        |
| `dependencyCheckRetireJSAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJsAnalyzerFilterNonVulnerable` | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJsAnalyzerFilters`             | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJSAnalyzerRepoJSUrl`           | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJsAnalyzerRepoPassword`        | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJsAnalyzerRepoUser`            | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJsAnalyzerRepoValidFor`        | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRetireJSForceUpdate`                 | Moved to `dependencyCheckAnalyzers` under the `retireJS` field.                      |
| `dependencyCheckRubygemsAnalyzerEnabled`             | Moved to `dependencyCheckAnalyzers` under the `ruby` field.                          |
| `dependencyCheckSwiftEnabled`                        | Moved to `dependencyCheckAnalyzers` under the `swift` field.                         |
| `dependencyCheckSwiftPackageResolvedAnalyzerEnabled` | Moved to `dependencyCheckAnalyzers` under the `swift` field.                         |
| `dependencyCheckYarnAuditAnalyzerEnabled`            | Moved to `dependencyCheckAnalyzers` under the `yarn` field.                          |
| `dependencyCheckZipExtensions`                       | Moved to `dependencyCheckAnalyzers` under the `additionalZipExtensions` field.       |


### Advanced Configuration

| Previous Setting                                 | New Setting                                                                                  |
|:-------------------------------------------------|:---------------------------------------------------------------------------------------------|
| `dependencyCheckConnectionReadTimeout`           | Same value.                                                                                  |
| `dependencyCheckConnectionString`                | Moved to `dependencyCheckDatabase`.                                                          |
| `dependencyCheckConnectionTimeout`               | Same value.                                                                                  |
| `dependencyCheckCvePassword`                     | Moved to `dependencyCheckNvdApi` under the `dataFeed` field (consider using to NVD API Key)  |
| `dependencyCheckCveStartYear`                    | Moved to `dependencyCheckNvdApi` under the `dataFeed` field (consider using to NVD API Key). |
| `dependencyCheckCveUrlBase`                      | Moved to `dependencyCheckNvdApi` under the `dataFeed` field (consider using to NVD API Key)  |
| `dependencyCheckCveUrlModified`                  | Moved to `dependencyCheckNvdApi` under the `dataFeed` field (consider using to NVD API Key)  |
| `dependencyCheckCveUser`                         | Moved to `dependencyCheckNvdApi` under the `dataFeed` field (consider using to NVD API Key)  |
| `dependencyCheckCveWaitTime`                     | N/A                                                                                          |
| `dependencyCheckDatabaseDriverName`              | Moved to `dependencyCheckDatabase`.                                                          |
| `dependencyCheckDatabaseDriverPath`              | Moved to `dependencyCheckDatabase`.                                                          |
| `dependencyCheckDatabasePassword`                | Moved to `dependencyCheckDatabase`.                                                          |
| `dependencyCheckDatabaseUser`                    | Moved to `dependencyCheckDatabase`.                                                          |
| `dependencyCheckDataDirectory`                   | Same value.                                                                                  |
| `dependencyCheckHostedSuppressionsEnabled`       | Moved to `dependencyCheckHostedSuppressions`.                                                |
| `dependencyCheckHostedSuppressionsForceUpdate`   | Moved to `dependencyCheckHostedSuppressions`.                                                |
| `dependencyCheckHostedSuppressionsUrl`           | Moved to `dependencyCheckHostedSuppressions`.                                                |
| `dependencyCheckHostedSuppressionsValidForHours` | Moved to `dependencyCheckHostedSuppressions`.                                                |
