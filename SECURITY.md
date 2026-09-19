# Security Policy

## Supported Versions

sbt-dependency-check is released from the `main` branch. Only the latest published release line
receives security fixes.

| Version | Supported          |
|---------|--------------------|
| 2.x     | :white_check_mark: |
| < 2.0   | :x:                |

The plugin wraps [OWASP dependency-check](https://github.com/dependency-check/DependencyCheck); the
vulnerability data and scanning engine it relies on come from that project. Keeping the plugin up to
date also keeps the bundled dependency-check-core current.
