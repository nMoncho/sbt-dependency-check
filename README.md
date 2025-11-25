# sbt-dependency-check [![Build Status](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml/badge.svg)](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml) [![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)

The sbt-dependency-check plugin allows projects to monitor dependent libraries for known, published vulnerabilities
(e.g. CVEs). The plugin achieves this by using the
awesome [OWASP DependencyCheck library](https://github.com/dependency-check/DependencyCheck)
which already offers several integrations with other build and continuous integration systems.

For more information on how OWASP DependencyCheck works and how to read the reports check
the [project's documentation](https://jeremylong.github.io/DependencyCheck/index.html).

This plugin is inspired by the great work of Alexander v. Buchholtz et
al. [sbt-dependency-check](https://github.com/albuch/sbt-dependency-check).
This plugin seeks to build on top of the previous plugin, keeping some settings and tasks the same, while offering some
functionalities on top. The work on this plugin started when we noticed NVD deprecating data-feed, which the previous
plugin still relied on. If you're looking to migrate from Buchholtz's plugin, please read
the [Migration Guide](MIGRATION.md)

## Installation

Add the plugin to your project configuration:

```scala
addSbtPlugin("net.nmoncho" % "sbt-dependency-check" % "1.8.4")
```

The minimum SBT version supported is `1.9.0`.

## Usage

### Getting Started

Don't feel deterred by all the configuration settings defined in this plugin. All of them have sensible defaults.

The best way to get started is to install the plugin, set your [NVD API Key](#nvd-api):

```sbt
import net.nmoncho.sbt.dependencycheck.settings._

dependencyCheckNvdApi := NvdApiSettings(apiKey = "YOUR_NVD_API_KEY")
```

And then just run:

```bash
sbt -Dlog4j2.level=info dependencyCheck
```

The first time you run these tasks it will take some time, even a couple of minutes. The analysis will write a report
to `target/{scala-version}/dependency-check-report.html` for SBT 1.x,
and `target/out/jvm/{scala-version}/{project}/dependency-check-report.html` for SBT 2.x. The plugin will log where the
reports are being written to.

After this, feel free to take a look at the available tasks and settings.

### Tasks

The following tasks are available:

| Task                                    | Description                                                                                                                                                               |
|:----------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `dependencyCheck`                       | Runs dependency-check against the project and generates a report per sub project.                                                                                         |
| `dependencyCheckAggregate`              | Runs dependency-check against project aggregates and combines the results into a single report.                                                                           |
| `dependencyCheckAllProjects`            | Runs dependency-check against all projects and combines the results into a single report.                                                                                 |
| `dependencyCheckUpdate`                 | Updates the local cache of the NVD data from NIST.                                                                                                                        |
| `dependencyCheckPurge`                  | Deletes the local copy of the NVD. This is used to force a refresh of the data.                                                                                           |
| `dependencyCheckListSettings`           | List the settings used during the analysis.                                                                                                                               |
| `dependencyCheckListUnusedSuppressions` | List unused suppressions, only considering suppression files or rules defined in the project definition (ie. build.sbt), not hosted suppressions nor packed suppressions. |
| `dependencyCheckListSuppressions`       | List suppression rules added to the Owasp Engine which are defined in the project definition (ie. build.sbt), or are imported packaged suppressions.                      |

The reports will be written to `crossTarget.value` by default. This can be overwritten by setting `dependencyCheckOutputDirectory`.
See [Configuration](#configuration) for details.

#### `dependencyCheck` Arguments

By default `dependencyCheck` will run under the selected project, or `root` if none is selected. And it will also run
on projects aggregated by that project, like any other task on SBT, generating one report per project.

The task `dependencyCheck` supports arguments that can be used to change its behavior:

- `list-settings`: The settings used for the analysis will be printed before running the analysis. This works the same
  way as the task `dependencyCheckListSettings`.
- `list-unused-suppressions`: Any unused suppression rule will be printed after the analysis. This works the same way as
  the task `dependencyCheckListUnusedSuppressions`.
- `single-report`: A single report will be generated for this project, and all aggregates if any. This works the same
  way as the task `dependencyCheckAggregate`.
- `all-projects`: A single report will be generated for all projects. This works the same way as the task `dependencyCheckAllProjects`.
  **Important**: This arguments needs to be used together with `single-report`.

This task also supports modifying how the reporting summary is shown at the end:

- `original-summary`: This is the original summary provided by previous version of the plugin. Follows the structure as
  `org.owasp.dependencycheck.agent.DependencyCheckScanAgent.showSummary`
- `all-vulnerabilities-summary`: Shows a more compact report than `original-summary`, but includes the score for each
  vulnerability.
- `offending-vulnerabilities-summary`: Same as `all-vulnerabilities-summary` but only shows the offending vulnerabilities
  (i.e. the ones that made the build fail).

### Configuration

The plugin uses the default [DependencyCheck](https://github.com/dependency-check/DependencyCheck) configuration which
can be overridden by either a SBT Setting Key, or a System Property. Properties are resolved by the library in this
order:
(1) `dependencycheck.properties` values , (2) SBT Setting Keys, (3) System Property. Last non-empty value wins.

The default `properties` file can be overridden with the Setting Key `dependencyCheckSettingsFile`. Most, if not all,
settings are picked up from the default DependencyCheck is defining. You can run the task `dependencyCheckListSettings`
to know what's the final value of each setting, and an example of this properties file's content.

SBT Setting Keys are usually wrapped with an `Option`. This is meant to allow keeping the default value, at the cost of
some configuration convenience.

| Setting                                | Description                                                                                                                                                                          | Default                                                                        |
|:---------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------|
| `dependencyCheckFailBuildOnCVSS`       | Specifies if the build should be failed if a CVSS score above a specified level is identified                                                                                        | 11.0 (never fails a build)                                                     |
| `dependencyCheckJUnitFailBuildOnCVSS`  | If using the jUnit, specifies the CVSS score that is considered a `test` failure when generating a jUnit style report                                                                | 0.0                                                                            |
| `dependencyCheckSkip`                  | Skips this project on the dependency-check analysis                                                                                                                                  | `false`                                                                        |
| `dependencyCheckScopes`                | What library dependency scopes are considered during the analysis                                                                                                                    | Compile = true, Test = false, Runtime = true, Provided = true, Optional = true |
| `dependencyCheckScanSet`               | An optional sequence of files that specify additional files and/or directories to analyze as part of the scan                                                                        | Standard Scala conventions                                                     |
| `dependencyCheckFormats`               | The report formats to be generated                                                                                                                                                   | `HTML`                                                                         |
| `dependencyCheckAnalysisTimeout`       | Set the analysis timeout.                                                                                                                                                            | 180 minutes (by DependencyCheck)                                               |
| `dependencyCheckOutputDirectory`       | The location to write the report(s).                                                                                                                                                 | `cross.target`                                                                 |
| `dependencyCheckAutoUpdate`            | Sets whether auto-updating of the NVD CVE/CPE, retireJS and hosted suppressions data is enabled.                                                                                     | `true`                                                                         |
| `dependencyCheckDataDirectory`         | Base path to use for the data directory (for embedded db and other cached resources from the Internet)                                                                               | `[JAR]/data/11.0`                                                              |
| `dependencyCheckSettingsFile`          | Where to look for the 'dependencycheck.properties' file                                                                                                                              | Resource `dependencycheck.properties`                                          |
| `dependencyCheckAnalyzers`             | Settings for the different analyzers used during the analysis                                                                                                                        | See [Analyzer Settings](#analyzer-settings)                                    |
| `dependencyCheckSuppressions`          | Combines a sequence of file paths, or URLs to the XML suppression files, with any hosted suppressions the analysis should be using. Suppressions are used to ignore false positives. | empty                                                                          |
| `dependencyCheckDatabase`              | Settings for the database used to hold the CVEs during the analysis.                                                                                                                 | See [Database Settings](#database-settings)                                    |
| `dependencyCheckNvdApi`                | Settings to contact the NVD API, such as API Key, Request Delay, Max Retries, etc.                                                                                                   | See [NVD API](#nvd-api)                                                        |
| `dependencyCheckProxy`                 | Settings to use a Proxy. Honors System Properties like `https.proxyHost`, `https.proxyPort`, etc.                                                                                    | See [Running behind a proxy](#running-behind-a-proxy)                          |
| `dependencyCheckConnectionTimeout`     | Sets the URL Connection Timeout (in milliseconds) used when downloading external data.                                                                                               | `10 seconds`                                                                   |
| `dependencyCheckConnectionReadTimeout` | Sets the URL Connection Read Timeout (in milliseconds) used when downloading external data.                                                                                          | `60 seconds`                                                                   |

#### Sensitive Configuration

`DependencyCheck` may use sensitive information like usernames, passwords, and Bearer Tokens. Although these could be
added as SBT Setting Keys this is discouraged in order to avoid committing sensitive information to your VCS. Here are
some options to that:

- Install this plugin globally under `~/.sbt/<version>/plugins.sbt`, then define these values on that file.
- Set the setting `dependencyCheckSettingsFile` using an external `dependencycheck.properties`.
- Use System Properties when running an SBT Task: `sbt -Danalyzer.central.password=12348765 dependencyCheck`

#### NVD API

Dependency-check has moved from using the NVD data-feed to the NVD API. It is **highly** encouraged to obtain an NVD API
Key;
see [Requesting an API Key](https://nvd.nist.gov/developers/request-an-api-key). Without an NVD API Key, updating will
be **extremely slow**.

The NVD API has enforced rate limits. If you are using a single API KEY and multiple builds occur you could hit the rate
limit and receive 403 errors. In a CI environment one must use a caching strategy, like caching the Database
(see [Database Settings](#database-settings)), or sharing the `Data Directory` between builds.

| Setting          | Description                                                           | Default         |
|:-----------------|:----------------------------------------------------------------------|:----------------|
| `apiKey`         | API Key for the NVD API                                               | ""              |
| `endpoint`       | NVD API Endpoint                                                      | `null`          |
| `requestDelay`   | delay between requests for the NVD API                                | `0`             |
| `maxRetryCount`  | the maximum number of retry requests for a single call to the NVD API | `30`            |
| `validForHours`  | control the skipping of the check for NVD updates                     | `4`             |
| `resultsPerPage` | control the results per page lower than NVD's default of 2000.        | `null` (`2000`) |

**Data Feed Settings**

| Setting        | Description                                                                                                                                                   | Default         |
|:---------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------|
| `url`          | URL for the NVD API Data Feed                                                                                                                                 | `null`          |
| `startYear`    | starting year for the NVD CVE Data feed cache.                                                                                                                | `null` (`2002`) |
| `validForDays` | indicates how often the NVD API data feed needs to be updated before a full refresh is evaluated                                                              | `7`             |
| `username`     | username to use when connecting to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Basic-authentication. | `null`          |
| `password`     | password to authenticate to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Basic-authentication.        | `null`          |
| `bearerToken`  | token to authenticate to the NVD Data feed. For use when NVD API Data is hosted as datafeeds locally on a site requiring HTTP-Bearer-authentication.          | `null`          |

#### Database Settings

| Setting              | Description                                                             | Default                                                                                      |
|:---------------------|:------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------|
| `driverName`         | the database driver class name. An embedded database is used by default | `org.h2.Driver`                                                                              |
| `driverPath`         | the database driver class path                                          | `null`                                                                                       |
| `connectionString`   | the database connection string                                          | `jdbc:h2:file:%s;AUTOCOMMIT=ON;CACHE_SIZE=65536;RETENTION_TIME=1000;MAX_COMPACT_TIME=10000;` |
| `username`           | username to use when connecting to the database                         | `dcuser`                                                                                     |
| `password`           | password to use when connecting to the database                         | <masked>                                                                                     |
| `batchInsertEnabled` | adds capabilities to batch insert. Tested on PostgreSQL and H2          | `true`                                                                                       |
| `batchInsertSize`    | Size of database batch inserts                                          | `1000`                                                                                       |

#### Suppression Settings

Suppressions can be specified either as suppression files, as hosted suppressions, or as a SBT Setting Key.
A suppression file can be either an actual file or a URL. Hosted suppression are specified with a URL. The different
between the two is that suppression files are meant to be project specific, whereas hosted suppression are meant,
or can be, more general. Hosted Suppressions are considered "base" suppressions, whereas suppression files are not.

Suppressions defined with the `suppressions` field on the `dependencyCheckSuppressions` key are created using the
`net.nmoncho.sbt.dependencycheck.settings.SuppressionRule` class, providing an alternative to defining suppressions with
XML files.

Whether this suppression is taken into account or not is governed by the Analyzer
Setting `vulnerabilitySuppressionEnabled`.
Another useful setting is `failOnUnusedSuppressionRule` which will fail the build if there is any non-base suppression
not
applied.

**Suppression Files**

| Setting       | Description                                                                                                                                             | Default |
|:--------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------|:--------|
| `files`       | files or urls to consider                                                                                                                               | <empty> |
| `user`        | the username used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Basic-authentication. | `null`  |
| `password`    | the password used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Basic-authentication. | `null`  |
| `bearerToken` | the token used when connecting to the suppressionFiles. For use when your suppressionFiles are hosted on a site requiring HTTP-Bearer-authentication.   | `null`  |

**Hosted Suppressions**

| Setting         | Description                                                                                                                                | Default                                                                               |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------|
| `enabled`       | whether the hosted suppressions file datasource is enabled                                                                                 | `true`                                                                                |
| `url`           | hosted suppressions file URL                                                                                                               | `https://jeremylong.github.io/DependencyCheck/suppressions/publishedSuppressions.xml` |
| `forceUpdate`   | whether the hosted suppressions file will be updated regardless of the autoupdate settings.                                                | `null` (`false`)                                                                      |
| `validForHours` | controls the skipping of the check for hosted suppressions file updates.                                                                   | `2`                                                                                   |
| `username`      | the hosted suppressions username. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Basic-authentication      | `null`                                                                                |
| `password`      | the hosted suppressions password. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Basic-authentication      | `null`                                                                                |
| `bearerToken`   | the hosted suppressions bearer token. For use when hosted suppressions are mirrored locally on a site requiring HTTP-Bearer-authentication | `null`                                                                                |

##### Packaged Suppressions

In order to avoid duplicating suppression rules between related projects, we can export suppressions rules defined in a
project, and then reuse those suppressions on downstream projects. For example, say you have a "commons" project that
includes a library with a CVE. And then another project including that "commons" project. If we run CI on both projects
with a dependency check, we'd have to define the same suppression rule in both projects, as "commons" would have this
dependency in its classpath, and the other project would also have it as a transitive dependency. If we could define the
suppression only in "commons", and then reuse it on downstream projects, we would save us a lot of copy/paste and
headaches.

We can use and export package suppressions by enabling with the `packagedEnabled` field in
the `dependencyCheckSuppressions` key.
By default, packaged suppressions rules are disabled.

**Using Packaged Suppressions**
To use exported packaged suppressions rules by other projects we need to whitelist what dependencies we'll accept
suppressions rules from. By default, all dependencies are blacklisted.

For example, imagine we only want to accept packaged suppressions from libraries published by Typesafe or Lightbend, we
would configure our builds like:

```scala
dependencyCheckSuppressions := SuppressionSettings(
  packagedEnabled = true,
  packagedFilter = PackageFilter.ofGav {
    case ("com.typesafe" | "com.lightbend", _, _) => true
    case _ => false
  }
)
```

There are other ways to define `PackageFilter`s that can filter each dependency available in the classpath.

**Exporting Packaged Suppressions**
We can export the suppression rules we define in a project by just enabling the packaged suppression rules. An XML
suppression
rules file will be created and treated as a managed resource (i.e. will be included in the packaged JAR).

Only the suppression rules defined in the `files` (non-URLs, just files) and the `suppressions` fields on the
`dependencyCheckSuppressions` key will be included in the packaged suppressions rules. The rationale is that `URLs`
defined
in the `files` field, or the hosted suppressions can be easily shared already.

Every packaged suppression rule will be marked as "base", meaning it won't show in the dependency check report, nor on
the
unused suppressions rules list. This is to avoid duplicating information on multiple projects.

#### Analyzer Settings

Analyzer settings are grouped together where they make sense. This is an attempt to make the Setting Keys offered by the
plugin a bit more readable and comprehensible.

To learn more see the
available [File Type Analyzers](https://jeremylong.github.io/DependencyCheck/analyzers/index.html).
Some analyzers may be enabled but marked as experimental, which may not run if `experimentalEnabled` is disabled.
If you don't care about a particular Analyzer, feel free to ignore it, leaving the default values as they are.

Settings are grouped by either analyzer, tool, or language:

| Setting                           | Description                                                                                             | Default |
|:----------------------------------|:--------------------------------------------------------------------------------------------------------|:--------|
| `additionalZipExtensions`         | additional file extensions to be treated like a ZIP files, the contents will be extracted and analyzed. | `None`  |
| `archiveEnabled`                  | whether or not the Archive analyzer is enabled.                                                         | `true`  |
| `artifactory`                     | Artifactory Settings.                                                                                   |         |
| `autoconfEnabled`                 | whether or not the autoconf analyzer should be used.                                                    | `true`  |
| `cmakeEnabled`                    | whether or not the CMake analyzer is enabled.                                                           | `true`  |
| `cpanFileEnabled`                 | whether or not the Perl CPAN File analyzer is enabled.                                                  | `true`  |
| `cpeEnabled`                      | whether or not the CPE analyzer is enabled.                                                             | `true`  |
| `cpeSuppressionEnabled`           | whether or not the CPE Suppression analyzer is enabled.                                                 | `true`  |
| `dartEnabled`                     | whether or not the Dart analyzer is enabled.                                                            | `true`  |
| `dependencyBundlingEnabled`       | whether or not the Dependency Bundling analyzer is enabled.                                             | `true`  |
| `dependencyMergingEnabled`        | whether or not the Dependency Merging analyzer is enabled.                                              | `true`  |
| `dotNet`                          | .NET Settings.                                                                                          |         |
| `elixir`                          | Elixir Settings                                                                                         |         |
| `experimentalEnabled`             | whether or not experimental analyzers are enabled.                                                      | `false` |
| `failOnUnusedSuppressionRule`     | whether the Unused Suppression Rule analyzer should fail if there are unused rules.                     | `false` |
| `falsePositiveEnabled`            | whether or not the False Positive analyzer is enabled.                                                  | `true`  |
| `filenameEnabled`                 | whether or not the Filename analyzer is enabled.                                                        | `true`  |
| `fileVersionEnabled`              | whether or not the File Version analyzer is enabled (reads the PE headers of DLL and EXE files).        | `true`  |
| `golang`                          | Golang Settings.                                                                                        |         |
| `hints`                           | Hints Settings.                                                                                         |         |
| `jarEnabled`                      | whether or not the JAR analyzer is enabled.                                                             | `true`  |
| `knownExploitedVulnerabilities`   | Known Exploited Vulnerabilities settings.                                                               |         |
| `mavenCentral`                    | Maven Central Settings                                                                                  |         |
| `mavenInstallEnabled`             | whether or not the Maven Install analyzer is enabled.                                                   | `true`  |
| `nexus`                           | Nexus Settings.                                                                                         |         |
| `node`                            | Node Settings                                                                                           |         |
| `nvdCveEnabled`                   | whether or not the NVD CVE analyzer is enabled.                                                         | `true`  |
| `openSslEnabled`                  | whether or not the OpenSSL analyzer is enabled.                                                         | `true`  |
| `php`                             | PHP Settings.                                                                                           |         |
| `pnmp`                            | PNPM Settings.                                                                                          |         |
| `python`                          | Python Settings.                                                                                        |         |
| `retiredEnabled`                  | whether or not the retired analyzers are enabled.                                                       | `false` |
| `retireJS`                        | RetireJS Settings.                                                                                      |         |
| `ruby`                            | Ruby Settings.                                                                                          |         |
| `swift`                           | Swift Settings.                                                                                         |         |
| `versionFilterEnabled`            | whether or not the Version Filter analyzer is enabled.                                                  | `true`  |
| `vulnerabilitySuppressionEnabled` | whether or not the Vulnerability Suppression analyzer is enabled.                                       | `true`  |
| `yarn`                            | Yarn Settings.                                                                                          |         |

Most of the settings here are picked up from either the default `dependencycheck.properties`, or from source, thus these
tables try to gather them as best effort.

##### Artifactory Settings

| Setting            | Description                                                  | Default                                                |
|:-------------------|:-------------------------------------------------------------|:-------------------------------------------------------|
| `enabled`          | whether or not Artifactory is enabled                        | `false`                                                |
| `url`              | Artifactory search URL                                       | `null`                                                 |
| `parallelAnalysis` | whether or not should use parallel processing                | `null` (`true`)                                        |
| `apiToken`         | Artifactory API token                                        | `null`                                                 |
| `username`         | Artifactory username                                         | `null`                                                 |
| `bearerToken`      | Artifactory bearer token                                     | `null`                                                 |
| `usesProxy`        | whether or not the proxy should be used to reach Artifactory | `null` (`true` if proxy is enabled, `false` otherwise) |

##### .NET Settings

| Setting                 | Description                                                        | Default |
|:------------------------|:-------------------------------------------------------------------|:--------|
| `assemblyEnabled`       | whether or not the .NET Assembly analyzer is enabled               | `true`  |
| `assemblyPath`          | The path to dotnet core, if available                              | `null`  |
| `nuspecEnabled`         | whether or not the .NET Nuspec analyzer is enabled                 | `true`  |
| `nugetConfEnabled`      | whether or not the .NET Nuget packages. config analyzer is enabled | `true`  |
| `libManEnabled`         | whether or not the Libman analyzer is enabled                      | `true`  |
| `msBuildProjectEnabled` | whether the .NET MSBuild Project analyzer is enabled               | `true`  |

##### Elixir Settings

| Setting           | Description                                             | Default              |
|:------------------|:--------------------------------------------------------|:---------------------|
| `mixAuditEnabled` | whether or not the Elixir mix audit analyzer is enabled | `true`               |
| `mixAuditPath`    | The path to mix_audit, if available                     | `null` (`mix_audit`) |

##### Golang Settings

| Setting                     | Description                                | Default       |
|:----------------------------|:-------------------------------------------|:--------------|
| `dependencyAnalyzerEnabled` | whether the Golang Dep analyzer is enabled | `true`        |
| `moduleAnalyzerEnabled`     | whether the Golang Mod analyzer is enabled | `true`        |
| `path`                      | The path to go, if available               | `null` (`go`) |

##### Hints Settings

| Setting     | Description                          | Default |
|:------------|:-------------------------------------|:--------|
| `enabled`   | whether the Hint analyzer is enabled | `true`  |
| `hintsFile` | path to the hints file               | `null`  |

##### Known Exploited Vulnerabilities Settings

| Setting         | Description                                                                                                                                                        | Default                                                                               |
|:----------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------|
| `enabled`       | whether the Known Exploited Vulnerabilities analyzer is enabled                                                                                                    | `true`                                                                                |
| `url`           | the URL to retrieve the Known Exploited Vulnerabilities                                                                                                            | `https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json` |
| `username`      | the known exploited vulnerabilities username. For use when known exploited vulnerabilities are mirrored locally on a site requiring HTTP-Basic-authentication      | `null`                                                                                |
| `password`      | the known exploited vulnerabilities password. For use when known exploited vulnerabilities are mirrored locally on a site requiring HTTP-Basic-authentication      | `null`                                                                                |
| `bearerToken`   | the known exploited vulnerabilities bearer token. For use when known exploited vulnerabilities are mirrored locally on a site requiring HTTP-Bearer-authentication | `null`                                                                                |
| `validForHours` | controls the skipping of the check for Known Exploited Vulnerabilities updates.                                                                                    | `24`                                                                                  |

##### Maven Central Settings

| Setting            | Description                                                                                                                                                                                     | Default                                      |
|:-------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------------------------------|
| `enabled`          | whether the Maven Central analyzer is enabled                                                                                                                                                   | `true`                                       |
| `url`              | the Maven Central search URL                                                                                                                                                                    | `https://search.maven.org/solrsearch/select` |
| `query`            | the Maven Central search query                                                                                                                                                                  | `%s?q=1:%s&wt=xml`                           |
| `usesCache`        | whether Maven Central search results will be cached                                                                                                                                             | `true`                                       |
| `retryCount`       | the Maven Central analyzer request retry count                                                                                                                                                  | `7`                                          |
| `parallelAnalysis` | whether the Maven Central analyzer should use parallel processing                                                                                                                               | `false`                                      |
| `username`         | the Username to obtain content from Maven Central. For use when the central content URL is reconfigured to a site requiring HTTP-Basic-authentication                                           | `null`                                       |
| `password`         | the Password to obtain content from Maven Central. For use when the central content URL is reconfigured to a site requiring HTTP-Basic-authentication                                           | `null`                                       |
| `bearerToken`      | the token to obtain content from Maven Central from an HTTP-Bearer-auth protected location. For use when the central content URL is reconfigured to a site requiring HTTP-Bearer-authentication | `null`                                       |

##### Nexus Settings

| Setting     | Description                             | Default                                          |
|:------------|:----------------------------------------|:-------------------------------------------------|
| `enabled`   | whether the Nexus analyzer is enabled   | `false`                                          |
| `url`       | the Nexus search URL                    | `https://repository.sonatype.org/service/local/` |
| `username`  | the Nexus search credentials username   | `null`                                           |
| `password`  | the Nexus search credentials password   | `null`                                           |
| `usesProxy` | whether to use the proxy to reach Nexus | `true`                                           |

##### Node Settings

| Setting                      | Description                                                   | Default                                               |
|:-----------------------------|:--------------------------------------------------------------|:------------------------------------------------------|
| `auditEnabled`               | whether the Node Audit analyzer is enabled                    | `true`                                                |
| `auditUrl`                   | the URL to the Node Audit API                                 | `https://registry.npmjs.org/-/npm/v1/security/audits` |
| `auditSkipDevDependencies`   | whether the Node Audit analyzer should skip devDependencies   | `null` (`false`)                                      |
| `auditUsesCache`             | whether node audit analyzer results will be cached            | `true`                                                |
| `packageEnabled`             | whether the Node Package analyzer is enabled                  | `true`                                                |
| `packageSkipDevDependencies` | whether the Node Package analyzer should skip devDependencies | `null` (`false`)                                      |
| `npmCpeEnabled`              | where the NPM CPE analyzer is enabled                         | `true`                                                |

##### Sonatype OSS Index Settings

| Setting                  | Description                                                                                                                    | Default                         |
|:-------------------------|:-------------------------------------------------------------------------------------------------------------------------------|:--------------------------------|
| `enabled`                | whether the Sonatype OSS Index analyzer is enabled                                                                             | `true`                          |
| `url`                    | the Sonatype OSS Index URL                                                                                                     | `https://ossindex.sonatype.org` |
| `batchSize`              | the Sonatype OSS batch-size                                                                                                    | `null` (128)                    |
| `requestDelay`           | the Sonatype OSS Request Delay. Amount of time in seconds to wait before executing a request against the Sonatype OSS Rest API | `null` (0)                      |
| `useCache`               | whether the Sonatype OSS Index should use a local cache                                                                        | `true`                          |
| `warnOnlyOnRemoteErrors` | only warning about Sonatype OSS Index remote errors instead of failing the request                                             | `null` (`false`)                |
| `username`               | the Sonatype OSS Index user                                                                                                    | `null`                          |
| `password`               | the Sonatype OSS Index password                                                                                                | `null`                          |

##### PHP Settings

| Setting                           | Description                                                          | Default          |
|:----------------------------------|:---------------------------------------------------------------------|:-----------------|
| `composerLockEnabled`             | whether the PHP composer lock file analyzer is enabled               | `true`           |
| `composerLockSkipDevDependencies` | whether the PHP composer lock file analyzer should skip dev packages | `null` (`false`) |

##### PNPM Settings

| Setting   | Description                          | Default         |
|:----------|:-------------------------------------|:----------------|
| `enabled` | whether the pnpm analyzer is enabled | `true`          |
| `path`    | the path to pnpm if available        | `null` (`pnpm`) |

##### Python Settings

| Setting               | Description                                         | Default |
|:----------------------|:----------------------------------------------------|:--------|
| `pipEnabled`          | whether the pip analyzer is enabled                 | `true`  |
| `pipFileEnabled`      | whether the pipfile analyzer is enabled             | `true`  |
| `distributionEnabled` | whether the Python Distribution analyzer is enabled | `true`  |
| `packageEnabled`      | whether the Python Package analyzer is enabled      | `true`  |
| `poetryEnabled`       | whether the Poetry analyzer is enabled              | `true`  |

##### RetireJS Settings

| Setting               | Description                                                                                                                                                                                | Default                                                                                    |
|:----------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------|
| `enabled`             | whether the RetireJS analyzer is enabled                                                                                                                                                   | `true`                                                                                     |
| `forceUpdate`         | whether the RetireJS repository will be updated regardless of the autoupdate settings                                                                                                      | `null` (`false`)                                                                           |
| `filters`             | whether the RetireJS analyzer file content filters                                                                                                                                         | `null` (<empty>)                                                                           |
| `filterNonVulnerable` | whether the RetireJS analyzer should filter out non-vulnerable dependencies                                                                                                                | `false`                                                                                    |
| `url`                 | the URL to the RetireJS repository                                                                                                                                                         | `https://raw.githubusercontent.com/Retirejs/retire.js/master/repository/jsrepository.json` |
| `username`            | the RetireJS Repository username. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Basic-authentication                                                           | `null`                                                                                     |
| `password`            | the RetireJS Repository password. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Basic-authentication                                                           | `null`                                                                                     |
| `bearerToken`         | the token to download the RetireJS JSON data from an HTTP-Bearer-auth protected location. For use when the RetireJS Repository is mirrored on a site requiring HTTP-Bearer-authentication. | `null`                                                                                     |
| `validForHours`       | to control the skipping of the check for CVE updates                                                                                                                                       | `24`                                                                                       |

##### Ruby Settings

| Setting                       | Description                                        | Default                                                  |
|:------------------------------|:---------------------------------------------------|:---------------------------------------------------------|
| `gemSpecEnabled`              | whether the Ruby Gemspec Analyzer is enabled       | `true`                                                   |
| `bundleAuditEnabled`          | whether the Ruby Bundler Audit analyzer is enabled | `true`                                                   |
| `bundleAuditPath`             | The path to bundle-audit, if available             | `null` (`bundle-audit`)                                  |
| `bundleAuditWorkingDirectory` | bundle-audit working directory                     | `null` (where `bundle-audit` is contained, may be `pwd`) |

##### Swift Settings

| Setting                  | Description                                            | Default |
|:-------------------------|:-------------------------------------------------------|:--------|
| `packageManagerEnabled`  | whether the SWIFT package manager analyzer is enabled  | `true`  |
| `packageResolvedEnabled` | whether the SWIFT package resolved analyzer is enabled | `true`  |
| `carthageEnabled`        | whether the carthage analyzer is enabled               | `true`  |
| `cocoapodsEnabled`       | whether the cocoapods analyzer is enabled              | `true`  |

##### Yarn Settings

| Setting   | Description                          | Default         |
|:----------|:-------------------------------------|:----------------|
| `enabled` | whether the Yarn analyzer is enabled | `true`          |
| `path`    | the path to Yarn if available        | `null` (`yarn`) |

### Running behind a proxy

SBT and `sbt-dependency-check` both honor the standard http and https proxy settings for the JVM.

```bash
sbt -Dhttp.proxyHost=proxy.example.com \
    -Dhttp.proxyPort=3218 \
    -Dhttp.proxyUser=username \
    -Dhttp.proxyPassword=password \
    -Dproxy.nonproxyhosts="localhost|http://www.google.com" \
    dependencyCheck
```

### Changing Log Level

Add `-Dlog4j2.level=<level>` when running a task, for example:

```bash
sbt -Dlog4j2.level=debug dependencyCheck
```

Replace `dependencyCheck` with the right [task name](#tasks) that you use for your project.
