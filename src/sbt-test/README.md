# Scripted tests

These are sbt [scripted](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html) tests. Each
subdirectory is a throwaway sbt project with a `test` script of task invocations and assertions, run
by `sbt scripted` (or `sbt +scripted` for the cross-build).

## Network and CVE database requirement

Unlike the `munit` unit tests, these run the full OWASP dependency-check engine
(`dependencyCheck`, `dependencyCheckAggregate`, `dependencyCheckAllProjects`), which needs a
populated CVE database to find the known-vulnerable dependencies the assertions expect. Because
`dependencyCheckAutoUpdate` defaults to `true` and the NVD data has a validity window, a run with a
missing or stale cache will reach the network:

- The NVD API is contacted to download or refresh the CVE data. Set an NVD API key via the
  `NVD_API_KEY` environment variable (used by the scripted `build.sbt` files) to avoid aggressive
  rate limiting. Without a key, updates are throttled and may fail.
- The OSS Index and RetireJS analyzers, enabled by default, reach Sonatype and GitHub during
  analysis.

Point `DATA_DIRECTORY` at a persistent folder to reuse the CVE cache across runs. In CI the cache is
restored from a previous run and the NVD API key is provided only on trusted (push and
`workflow_dispatch`) builds, not on pull-request builds. Consequently these tests are slow on a cold
cache and can be nondeterministic if the cache has aged past its validity window.

## Related unit test

`DbSuite` (`src/test/scala/.../tasks/DbSuite.scala`) is the integration test that populates the
shared CVE cache. It runs only when `CI`, `FORCE_REFRESH`, `DATA_DIRECTORY`, and `NVD_API_KEY` are
all set (the cache-generation workflow); otherwise it reports as skipped rather than passing without
asserting anything.
