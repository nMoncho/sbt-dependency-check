# Migration guide

## Upgrading to 3.0.0

To migrate your project from v2.x to v3.x:

1. All `dependencyCheck` tasks, like `dependencyCheckAggregate`, have been unified under one task.
2. Use `configure` method instead of `apply` on Settings case classes.
3. The task `dependencyCheckListUnusedSuppressions` has been removed.

### Unified Tasks

All dependency check tasks have been unified under `dependencyCheck`. We need to use arguments to get
the same behavior as the previously available tasks:

- `dependencyCheckAggregate`: Use `dependencyCheck single-report`
- `dependencyCheckAllProjects`: Use `dependencyCheck single-report all-projects`

### Method `configure` on Settings case classes

On v1.x and v2.x whenever we wanted to configure any of the Setting case classes, like `DatabaseSettings`,
we'd have to `copy` the default instance. To streamline this, now we can create one instance right away,
and rely on default arguments for the values that aren't provided.

In turn, instead of using `apply` we provide `configure` as a way to create instances to disambiguate what
are we intending to do.

### Task `dependencyCheckListUnusedSuppressions` has been removed

Since this task actually runs an analysis, we remove this task and ask users to use the argument
`--list-unused-suppressions` on the task `dependencyCheck`.

## Upgrading to 2.0.0

Version 2.0.0 upgrades the bundled OWASP dependency-check engine from 12.2.2 to
[13.0.0](https://github.com/dependency-check/DependencyCheck/releases) and removes one deprecated
proxy setting. The typed settings DSL is otherwise unchanged from 1.x, so most builds upgrade with
no edits.

### Breaking changes

- **`ProxySettings.disableSchemas` was removed.** It had been deprecated in 1.x and no longer exists
  in dependency-check 13.0.0 (the `jdk.http.auth.tunneling.disabledSchemes` toggle is gone). If your
  build set it, drop the argument:

  ```diff
  - dependencyCheckProxy := ProxySettings(disableSchemas = Some(true), nonProxyHosts = Some(Seq("localhost")))
  + dependencyCheckProxy := ProxySettings(nonProxyHosts = Some(Seq("localhost")))
  ```

  If you never configured `dependencyCheckProxy`, or never used `disableSchemas`, no change is needed.

### The local NVD database

dependency-check 13.0.0 manages its own embedded database under a version-specific directory, so the
first run after upgrading may rebuild the local NVD data. That rebuild needs network access and,
ideally, an [NVD API key](README.md#nvd-api) to avoid rate limiting. In CI, refresh any cache that
was keyed to the old database so the new engine does not read a stale one; see the caching recipe in
[NVD API](README.md#nvd-api).

### Everything else

All other settings keep the same names and typed case-class DSL as 1.x. See
[Configuration](README.md#configuration) for the current settings. Every setting has a sensible
default, so a minimal build only needs an NVD API key.

If you are coming from a different sbt dependency-check plugin, there is no automatic key mapping:
configure this plugin through its typed settings DSL described in
[Configuration](README.md#configuration).
