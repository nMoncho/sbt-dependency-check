# Contributing

Thanks for your interest in improving `sbt-dependency-check`. This document describes how to build,
test, and style the project.

## Prerequisites

- A JDK (the build compiles the Scala 2.12 artifact for Java 11 and the Scala 3 artifact for Java
  17, so a JDK 17 or newer is recommended for local development).
- [sbt](https://www.scala-sbt.org/).

## Cross-building

The plugin is cross-built for two targets:

- Scala 2.12 on sbt 1.x
- Scala 3 on sbt 2.0

Run a task for the default (2.12) version directly, for every cross version with a `+` prefix, or for
one specific version with `++`:

```bash
sbt test          # Scala 2.12
sbt +test         # all cross versions
sbt ++3.8.4 test  # Scala 3 only
```

Any change to shared code under `src/main/scala` must compile on both targets.

## Tests

- **Unit tests** (`munit`) are offline and fast:

  ```bash
  sbt +test
  ```

- **Scripted tests** run the plugin end to end against a real OWASP engine and need a populated CVE
  database (and, ideally, network access plus an NVD API key). See
  [`src/sbt-test/README.md`](src/sbt-test/README.md) for details.

  ```bash
  sbt +scripted
  ```

- **Coverage** is measured with scoverage and gated by a minimum. Generate the report with:

  ```bash
  sbt testCoverage
  ```

  `testCoverage` runs `clean`, `+test`, `+scripted`, and the coverage report; it fails if coverage
  drops below the configured floors.

## Style

Formatting (scalafmt), lint/rewrites (scalafix), and license headers are enforced. Two aliases wrap
the tooling:

```bash
sbt styleCheck   # verify formatting, headers, and scalafix (used in CI)
sbt styleFix     # apply formatting, headers, and scalafix rewrites
```

Run `styleFix` before opening a pull request so `styleCheck` passes in CI.

## Commits and pull requests

- This project uses [Conventional Commits](https://www.conventionalcommits.org/) (for example
  `fix:`, `feat:`, `docs:`, `chore:`); a breaking change is marked with `!` (for example `chore!:`).
- Keep pull requests focused.
