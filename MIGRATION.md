# Migration guide

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
