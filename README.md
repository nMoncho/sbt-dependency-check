# sbt-dependency-check [![Build Status](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml/badge.svg)](https://github.com/nMoncho/sbt-dependency-check/actions/workflows/main.yaml) [![MIT License](https://img.shields.io/badge/license-MIT-blue.svg)](https://mit-license.org/)

The sbt-dependency-check plugin allows projects to monitor dependent libraries for known, published vulnerabilities
(e.g. CVEs). The plugin achieves this by using the
awesome [OWASP DependencyCheck library](https://github.com/dependency-check/DependencyCheck)
which already offers several integrations with other build and continuous integration systems.

In an attempt to keep this `README.md` brief, the detailed documentation on how to use this plugin
can be located in our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki).

## Installation

Add the plugin to your project configuration:

```scala
addSbtPlugin("net.nmoncho" % "sbt-dependency-check" % "1.9.0")
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

In a CI environment one must use a caching strategy, like caching the CVE Database.

Feel read more about this on our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki/NVD-API)

#### Suppression Settings

Due to [how dependency-check identifies libraries](https://dependency-check.github.io/DependencyCheck/general/internals.html)
false positives may occur (i.e. a CPE was identified that is incorrect). `sbt-dependency-check` offer several ways to
define these suppressions.

Feel read more about this on our [wiki](https://github.com/nMoncho/sbt-dependency-check/wiki/SUPPRESSIONS).

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
