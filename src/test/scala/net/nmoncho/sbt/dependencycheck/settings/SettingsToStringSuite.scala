/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

/** Verifies that the secret-bearing settings case classes redact their secrets in `toString`, so
  * `show <settingKey>` and any diagnostic logging never print credentials, while non-secret fields
  * (which also guard the redaction is applied to the right positions) remain visible.
  */
class SettingsToStringSuite extends munit.FunSuite {

  private def assertRedacted(rendered: String, secrets: Seq[String], visible: Seq[String]): Unit = {
    secrets.foreach(s =>
      assert(!rendered.contains(s), s"secret [$s] must not appear in: $rendered")
    )
    assert(rendered.contains("********"), s"expected a redaction marker in: $rendered")
    visible.foreach(v =>
      assert(rendered.contains(v), s"non-secret [$v] should appear in: $rendered")
    )
  }

  test("NvdApiSettings redacts the apiKey") {
    assertRedacted(
      NvdApiSettings(apiKey = "nvd-secret", endpoint = Some("https://nvd.example")).toString,
      secrets = Seq("nvd-secret"),
      visible = Seq("https://nvd.example")
    )
  }

  test("NvdApiSettings.DataFeed redacts password and bearerToken") {
    assertRedacted(
      NvdApiSettings
        .DataFeed(
          username    = Some("feed-user"),
          password    = Some("feed-pass"),
          bearerToken = Some("feed-tok")
        )
        .toString,
      secrets = Seq("feed-pass", "feed-tok"),
      visible = Seq("feed-user")
    )
  }

  test("DatabaseSettings redacts the password") {
    assertRedacted(
      DatabaseSettings(username = Some("db-user"), password = Some("db-pass")).toString,
      secrets = Seq("db-pass"),
      visible = Seq("db-user")
    )
  }

  test("HostedSuppressionsSettings redacts password and bearerToken") {
    assertRedacted(
      HostedSuppressionsSettings(
        username    = Some("h-user"),
        password    = Some("h-pass"),
        bearerToken = Some("h-tok")
      ).toString,
      secrets = Seq("h-pass", "h-tok"),
      visible = Seq("h-user")
    )
  }

  test("SuppressionFilesSettings redacts password and bearerToken") {
    assertRedacted(
      new SuppressionFilesSettings(
        Seq.empty,
        Some("sf-user"),
        Some("sf-pass"),
        Some("sf-tok")
      ).toString,
      secrets = Seq("sf-pass", "sf-tok"),
      visible = Seq("sf-user")
    )
  }

  test("AnalyzerSettings.Artifactory redacts apiToken and bearerToken") {
    assertRedacted(
      AnalyzerSettings
        .Artifactory(
          apiToken    = Some("art-tok"),
          username    = Some("art-user"),
          bearerToken = Some("art-bear")
        )
        .toString,
      secrets = Seq("art-tok", "art-bear"),
      visible = Seq("art-user")
    )
  }

  test("AnalyzerSettings.KnownExploitedVulnerabilities redacts password and bearerToken") {
    assertRedacted(
      AnalyzerSettings
        .KnownExploitedVulnerabilities(
          username    = Some("kev-user"),
          password    = Some("kev-pass"),
          bearerToken = Some("kev-tok")
        )
        .toString,
      secrets = Seq("kev-pass", "kev-tok"),
      visible = Seq("kev-user")
    )
  }

  test("AnalyzerSettings.MavenCentral redacts password and bearerToken") {
    assertRedacted(
      AnalyzerSettings
        .MavenCentral(
          username    = Some("mc-user"),
          password    = Some("mc-pass"),
          bearerToken = Some("mc-tok")
        )
        .toString,
      secrets = Seq("mc-pass", "mc-tok"),
      visible = Seq("mc-user")
    )
  }

  test("AnalyzerSettings.Nexus redacts the password") {
    assertRedacted(
      AnalyzerSettings.Nexus(username = Some("nx-user"), password = Some("nx-pass")).toString,
      secrets = Seq("nx-pass"),
      visible = Seq("nx-user")
    )
  }

  test("AnalyzerSettings.OssIndex redacts the password") {
    assertRedacted(
      AnalyzerSettings.OssIndex(username = Some("oss-user"), password = Some("oss-pass")).toString,
      secrets = Seq("oss-pass"),
      visible = Seq("oss-user")
    )
  }

  test("AnalyzerSettings.RetireJS redacts password and bearerToken") {
    assertRedacted(
      AnalyzerSettings
        .RetireJS(
          username    = Some("rjs-user"),
          password    = Some("rjs-pass"),
          bearerToken = Some("rjs-tok")
        )
        .toString,
      secrets = Seq("rjs-pass", "rjs-tok"),
      visible = Seq("rjs-user")
    )
  }
}
