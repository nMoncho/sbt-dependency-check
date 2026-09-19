# sbt-dependency-check [![Build Status](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml/badge.svg)](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml) [![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/) [![sbt-dependency-check - sbt 1.x](https://index.scala-lang.org/nmoncho/sbt-dependency-check/sbt-dependency-check/latest-by-scala-version.svg?platform=sbt1)](https://index.scala-lang.org/nmoncho/sbt-dependency-check/sbt-dependency-check) [![sbt-dependency-check - sbt 2.x](https://index.scala-lang.org/nmoncho/sbt-dependency-check/sbt-dependency-check/latest-by-scala-version.svg?platform=sbt2)](https://index.scala-lang.org/nmoncho/sbt-dependency-check/sbt-dependency-check)

The sbt-dependency-check plugin allows projects to monitor dependent libraries for known, published vulnerabilities
(e.g. CVEs). The plugin achieves this by using the
awesome [OWASP DependencyCheck library](https://github.com/dependency-check/DependencyCheck)
which already offers several integrations with other build and continuous integration systems.

In an attempt to keep this `README.md` brief, the detailed documentation on how to use this plugin
can be located in our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki).

## Installation

Add the plugin to your project configuration:

```scala
addSbtPlugin("net.nmoncho" % "sbt-dependency-check" % "2.0.0")
```

The minimum SBT version supported is `1.9.0`.

## Usage

### Getting Started

Don't feel deterred by all the configuration settings defined in this plugin. All of them have sensible defaults.

The best way to get started is to install the plugin, set your [NVD API Key](#nvd-api):

```sbt
import net.nmoncho.sbt.dependencycheck.settings._

// Read the NVD API key from an environment variable so it is never committed to VCS.
dependencyCheckNvdApi := sys.env
  .get("NVD_API_KEY")
  .map(key => NvdApiSettings(key))
  .getOrElse(NvdApiSettings.Default)
```

Do not hardcode the key in a VCS-tracked `build.sbt`; see [Sensitive Configuration](#sensitive-configuration)
for ways to keep it (and other credentials) out of source control.

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
| `dependencyCheckGenerateSuppressions`   | Runs the analysis and writes a suppression XML baseline of the vulnerabilities currently found, so an existing project can baseline known findings and fail only on new ones. |

The reports will be written to `crossTarget.value` by default. This can be overwritten by setting `dependencyCheckOutputDirectory`.
See [Configuration](#configuration) for details.

`dependencyCheckGenerateSuppressions` writes `dependency-check-suppressions.xml` into `dependencyCheckOutputDirectory`, with one
`<suppress>` entry per vulnerable dependency (targeted by Package URL, SHA1, or file path) listing the CVEs found. Review the
file, then wire it in via `dependencyCheckSuppressions` to baseline existing findings so only new vulnerabilities fail the build.
The entries are editable, so you can add an `until` date to make a suppression temporary or trim CVEs you do not want to ignore.

#### `dependencyCheck` Arguments

By default `dependencyCheck` will run under the selected project, or `root` if none is selected. And it will also run
on projects aggregated by that project, like any other task on SBT, generating one report per project.

> **Performance on multi-module builds.** Because `dependencyCheck` produces one report per project, it initializes a
> separate OWASP engine for each module. The fixed per-engine cost (engine construction, analyzer loading, opening the
> local NVD database, and the update freshness check) is therefore paid once per module. On builds with many modules this
> can dominate the wall-clock time. If a single combined report is acceptable, prefer `dependencyCheckAggregate` or
> `dependencyCheckAllProjects` (equivalently, the `single-report` or `all-projects` arguments below): they analyze all
> collected dependencies with a single shared engine and pay that fixed cost only once. Note that the NVD data itself is
> not downloaded more than once regardless of the task, since the local cache is reused across engines.

The task `dependencyCheck` supports arguments that can be used to change its behavior:

- `list-settings`: The settings used for the analysis will be printed before running the analysis. This works the same
  way as the task `dependencyCheckListSettings`.
- `list-unused-suppressions`: Any unused suppression rule will be printed after the analysis. This works the same way as
  the task `dependencyCheckListUnusedSuppressions`.
- `single-report`: A single report will be generated for this project, and all aggregates if any. This works the same
  way as the task `dependencyCheckAggregate`.
- `all-projects`: A single report will be generated for all projects. This works the same way as the task `dependencyCheckAllProjects`.
  A single combined report is always produced for all projects, so `single-report` is implied and does not need to be passed alongside it.

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
| `dependencyCheckFailOnCves`            | CVE ids (e.g. `CVE-2021-44228`) that must always fail the build when found, regardless of their CVSS score                                                                           | empty                                                                          |
| `dependencyCheckFailOnKnownExploited`  | Fail the build when any dependency has a Known Exploited Vulnerability (KEV), regardless of its CVSS score                                                                           | `false`                                                                        |
| `dependencyCheckWarnOnly`              | Report vulnerabilities that would fail the build (summary and report) without failing the build                                                                                     | `false`                                                                        |
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

> **Build gating:** `dependencyCheckFailBuildOnCVSS` defaults to `11.0`, so out of the box the build is never failed by a vulnerability (CVSS scores range from 0 to 10). To gate CI on findings, set a starting threshold such as `dependencyCheckFailBuildOnCVSS := 7.0` (fail on High and Critical), and/or use the policy settings `dependencyCheckFailOnKnownExploited := true` and `dependencyCheckFailOnCves := Seq("CVE-...")`. Use `dependencyCheckWarnOnly := true` to surface findings without failing the build while you tune a policy.

#### Report Formats

`dependencyCheckFormats` selects which reports are written. The default is `HTML` only, which is convenient
for humans but not for CI ingestion. The available formats are re-exported through the plugin's `Format`
value, so no OWASP import is needed:

```scala
dependencyCheckFormats := Seq(Format.HTML, Format.SARIF)
```

Available formats: `HTML`, `XML`, `JSON`, `CSV`, `JUNIT`, `SARIF`, `JENKINS`, `GITLAB`, `ALL`.

For CI, generate a machine-readable format alongside `HTML`:

- **GitHub code scanning**: generate `SARIF` and upload `dependency-check-report.sarif` with the
  `github/codeql-action/upload-sarif` action, so findings appear in the repository's Security tab.
- **GitLab dependency scanning**: generate `GITLAB` to produce a report GitLab can ingest.
- **Generic CI or dashboards**: generate `JSON` or `XML` for programmatic processing.

#### Common Examples

The nested settings (`dependencyCheckAnalyzers`, `dependencyCheckNvdApi`, `dependencyCheckDatabase`, ...) are
case classes whose companion `apply` defaults every field, so you can set a single field while keeping the
rest at their defaults, without reconstructing the whole tree.

Enable the experimental analyzers (a top-level toggle):

```scala
dependencyCheckAnalyzers := AnalyzerSettings(experimentalEnabled = Some(true))
```

Toggle a nested analyzer, for example turn off the Node Audit analyzer:

```scala
dependencyCheckAnalyzers := AnalyzerSettings(node = AnalyzerSettings.Node(auditEnabled = Some(false)))
```

Cache the NVD data in a fixed directory and run offline in CI. Populate the cache once with
`dependencyCheckUpdate`, then disable auto-updates so the analysis never reaches the network:

```scala
dependencyCheckDataDirectory := Some((ThisBuild / baseDirectory).value / ".dependency-check-data")
dependencyCheckAutoUpdate := false
```

To change several fields at once, `.copy` an existing value (for example the default):

```scala
dependencyCheckNvdApi := NvdApiSettings.Default.copy(maxRetryCount = Some(50))
```

#### Sensitive Configuration

`DependencyCheck` may use sensitive information like the NVD API key, usernames, passwords, and Bearer Tokens. Although
these could be added as SBT Setting Keys this is discouraged in order to avoid committing sensitive information to your
VCS. Here are some options to that:

- Read the value from the environment, as the [Getting Started](#getting-started) example does for the NVD API key:
  `dependencyCheckNvdApi := sys.env.get("NVD_API_KEY").map(NvdApiSettings(_)).getOrElse(NvdApiSettings.Default)`.
- Install this plugin globally under `~/.sbt/<version>/plugins.sbt`, then define these values on that file.
- Set the setting `dependencyCheckSettingsFile` using an external `dependencycheck.properties`.
- Use System Properties when running an SBT Task: `sbt -Danalyzer.central.password=12348765 dependencyCheck`

The NVD API key is low sensitivity (it is free and revocable), but keeping it out of VCS is still good practice and
avoids leaking it through your git history.

#### NVD API

Dependency-check has moved from using the NVD data-feed to the NVD API. It is **highly** encouraged to obtain an NVD API
Key;
see [Requesting an API Key](https://nvd.nist.gov/developers/request-an-api-key). Without an NVD API Key, updating will
be **extremely slow**.

In a CI environment one must use a caching strategy, like caching the CVE Database.

Feel read more about this on our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki/NVD-API)

#### Suppression Settings

Due to [how dependency-check identifies libraries](https://dependency-check.github.io/DependencyCheck/general/internals.html)
false positives may occur (i.e. a CPE was identified that is incorrect). `sbt-dependency-check` offer several ways to
define these suppressions.

Feel read more about this on our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki/SUPPRESSIONS).

> **Trust boundary for packaged suppressions.** When `packagedEnabled` is turned on, suppression
> rules shipped inside your dependencies' JARs (`packaged-suppressions-file.xml`) are imported into
> the scan. Such a rule can suppress arbitrary CVEs, including ones in other dependencies, so a
> packaged suppression is as trusted as the code of the dependency that ships it. The default filter
> is `BlacklistAll` (packaged suppressions from no dependency are trusted); prefer an explicit
> per-GAV allowlist over `WhitelistAll`, which trusts every dependency's suppressions. Imported
> packaged rules are marked `base`, so they do not appear in the report's "suppressed" section; the
> plugin logs, at info level, which dependency contributed how many rules, and
> `dependencyCheckListSuppressions` lists them.

#### Analyzer Settings

Analyzers, as the name imply, are a way to analyze dependencies or artifacts.
[DependencyCheck](https://github.com/dependency-check/DependencyCheck) offers an extensive
list of analyzers out of the box.

Feel read more about this on our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki/ANALYZERS).

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

### Dependency Footprint

This plugin runs [OWASP dependency-check](https://github.com/dependency-check/DependencyCheck)
in-process, so `dependency-check-core` and its (large) transitive dependency tree, including Lucene,
an embedded H2 database, the Jackson stack, Guava, Apache HttpClient 5, and several Apache Commons
libraries, are added to the sbt meta-build (`project/`) plugin classpath. They are never added to
your application or runtime classpath and cannot affect your published artifacts. The cost is a
larger plugin download and, occasionally, an eviction warning when another sbt plugin in the same
build needs a different version of a shared library such as Jackson or Guava. This footprint is
inherent to running OWASP dependency-check in-process (the Maven and Gradle integrations carry it
too).

If a conflict on the plugin classpath produces eviction warnings, or an eviction error under sbt 2,
resolve it in your own `project/plugins.sbt` (the meta-build that hosts the plugin). For example, to
downgrade a spurious eviction error to a warning:

```scala
// project/plugins.sbt
ThisBuild / libraryDependencySchemes += "com.fasterxml.jackson.core" % "jackson-databind" % "always"
```

or to pin a specific version across the plugin classpath:

```scala
// project/plugins.sbt
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "<version-you-need>"
```

Setting these in your regular `build.sbt` has no effect here, because the conflict is on the
meta-build classpath rather than your application's.
