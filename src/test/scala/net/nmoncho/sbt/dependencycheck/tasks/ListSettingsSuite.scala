/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.tasks

import net.nmoncho.sbt.dependencycheck.Utils.StringLogger
import net.nmoncho.sbt.dependencycheck.settings.ScopesSettings
import org.owasp.dependencycheck.utils.Settings

class ListSettingsSuite extends munit.FunSuite {

  test("Settings can be listed") {
    implicit val log: StringLogger = new StringLogger

    ListSettings(new Settings(), ScopesSettings.Default)

    val result = log.sb.result()
    assertEquals(result.trim, expected.trim)

    assert(result.contains("data.password: ********"), "sensitive settings are masked properly")
  }

  private lazy val expected =
    """	ScopesSettings:
      |	  compile: true
      |	  test: false
      |	  runtime: true
      |	  provided: true
      |	  optional: true
      |	analyzer.archive.enabled: true
      |	analyzer.artifactory.api.token: null
      |	analyzer.artifactory.api.username: null
      |	analyzer.artifactory.bearer.token: null
      |	analyzer.artifactory.enabled: false
      |	analyzer.artifactory.parallel.analysis: null
      |	analyzer.artifactory.proxy: null
      |	analyzer.artifactory.url: null
      |	analyzer.assembly.dotnet.path: null
      |	analyzer.assembly.enabled: true
      |	analyzer.autoconf.enabled: true
      |	analyzer.bundle.audit.enabled: true
      |	analyzer.bundle.audit.path: null
      |	analyzer.bundle.audit.working.directory: null
      |	analyzer.carthage.enabled: true
      |	analyzer.central.bearertoken: null
      |	analyzer.central.enabled: true
      |	analyzer.central.parallel.analysis: false
      |	analyzer.central.password: null
      |	analyzer.central.query: %s?q=1:%s&wt=xml
      |	analyzer.central.retry.count: 7
      |	analyzer.central.url: https://search.maven.org/solrsearch/select
      |	analyzer.central.use.cache: true
      |	analyzer.central.username: null
      |	analyzer.cmake.enabled: true
      |	analyzer.cocoapods.enabled: true
      |	analyzer.composer.lock.enabled: true
      |	analyzer.composer.lock.skipdev: null
      |	analyzer.cpanfile.enabled: true
      |	analyzer.cpe.enabled: true
      |	analyzer.cpesuppression.enabled: true
      |	analyzer.dart.enabled: true
      |	analyzer.dependencybundling.enabled: true
      |	analyzer.dependencymerging.enabled: true
      |	analyzer.experimental.enabled: false
      |	analyzer.falsepositive.enabled: true
      |	analyzer.filename.enabled: true
      |	analyzer.golang.dep.enabled: true
      |	analyzer.golang.mod.enabled: true
      |	analyzer.golang.path: null
      |	analyzer.hint.enabled: true
      |	analyzer.jar.enabled: true
      |	analyzer.knownexploited.enabled: true
      |	analyzer.libman.enabled: true
      |	analyzer.maveninstall.enabled: true
      |	analyzer.mix.audit.enabled: true
      |	analyzer.mix.audit.path: null
      |	analyzer.msbuildproject.enabled: true
      |	analyzer.nexus.enabled: false
      |	analyzer.nexus.password: null
      |	analyzer.nexus.proxy: true
      |	analyzer.nexus.url: https://repository.sonatype.org/service/local/
      |	analyzer.nexus.username: null
      |	analyzer.node.audit.enabled: true
      |	analyzer.node.audit.skipdev: null
      |	analyzer.node.audit.url: https://registry.npmjs.org/-/npm/v1/security/audits
      |	analyzer.node.audit.use.cache: true
      |	analyzer.node.package.enabled: true
      |	analyzer.node.package.skipdev: null
      |	analyzer.npm.cpe.enabled: true
      |	analyzer.nugetconf.enabled: true
      |	analyzer.nuspec.enabled: true
      |	analyzer.nvdcve.enabled: true
      |	analyzer.openssl.enabled: true
      |	analyzer.ossindex.batch.size: null
      |	analyzer.ossindex.enabled: true
      |	analyzer.ossindex.password: null
      |	analyzer.ossindex.remote-error.warn-only: null
      |	analyzer.ossindex.request.delay: null
      |	analyzer.ossindex.url: https://ossindex.sonatype.org
      |	analyzer.ossindex.use.cache: true
      |	analyzer.ossindex.user: null
      |	analyzer.pe.enabled: true
      |	analyzer.pip.enabled: true
      |	analyzer.pipfile.enabled: true
      |	analyzer.pnpm.audit.enabled: true
      |	analyzer.pnpm.path: null
      |	analyzer.poetry.enabled: true
      |	analyzer.python.distribution.enabled: true
      |	analyzer.python.package.enabled: true
      |	analyzer.retired.enabled: false
      |	analyzer.retirejs.enabled: true
      |	analyzer.retirejs.filternonvulnerable: false
      |	analyzer.retirejs.filters: null
      |	analyzer.retirejs.forceupdate: null
      |	analyzer.retirejs.repo.js.bearertoken: null
      |	analyzer.retirejs.repo.js.password: null
      |	analyzer.retirejs.repo.js.url: https://raw.githubusercontent.com/Retirejs/retire.js/master/repository/jsrepository.json
      |	analyzer.retirejs.repo.js.username: null
      |	analyzer.retirejs.repo.validforhours: 24
      |	analyzer.ruby.gemspec.enabled: true
      |	analyzer.suppression.unused.fail: false
      |	analyzer.swift.package.manager.enabled: true
      |	analyzer.swift.package.resolved.enabled: true
      |	analyzer.versionfilter.enabled: true
      |	analyzer.vulnerabilitysuppression.enabled: true
      |	analyzer.yarn.audit.enabled: true
      |	analyzer.yarn.path: null
      |	central.content.bearertoken: null
      |	central.content.password: null
      |	central.content.url: https://search.maven.org/remotecontent?filepath=
      |	central.content.username: null
      |	connection.read.timeout: null
      |	connection.timeout: null
      |	cpe.url: https://static.nvd.nist.gov/feeds/xml/cpe/dictionary/official-cpe-dictionary_v2.3.xml.gz
      |	cpe.validfordays: 30
      |	cve.cpe.startswith.filter: cpe:2.3:a:
      |	data.connection_string: jdbc:h2:file:%s;AUTOCOMMIT=ON;CACHE_SIZE=65536;RETENTION_TIME=1000;MAX_COMPACT_TIME=10000;
      |	data.directory: [JAR]/data/11.0
      |	data.driver_name: org.h2.Driver
      |	data.driver_path: null
      |	data.file_name: odc.mv.db
      |	data.h2.directory: null
      |	data.password: ********
      |	data.user: dcuser
      |	data.version: 5.5
      |	data.writelock.shutdownhook: org.owasp.dependencycheck.utils.WriteLockCleanupHook
      |	database.batchinsert.enabled: true
      |	database.batchinsert.maxsize: 1000
      |	downloader.quick.query.timestamp: true
      |	downloader.tls.protocols: TLSv1.1,TLSv1.2,TLSv1.3
      |	ecosystem.skip.cpeanalyzer: npm
      |	engine.version.url: https://dependency-check.github.io/DependencyCheck/current.txt
      |	extensions.zip: null
      |	hints.file: null
      |	hosted.suppressions.bearertoken: null
      |	hosted.suppressions.enabled: true
      |	hosted.suppressions.forceupdate: null
      |	hosted.suppressions.password: null
      |	hosted.suppressions.url: https://dependency-check.github.io/DependencyCheck/suppressions/publishedSuppressions.xml
      |	hosted.suppressions.user: null
      |	hosted.suppressions.validforhours: 2
      |	junit.fail.on.cvss: 0
      |	kev.bearertoken: null
      |	kev.check.validforhours: 24
      |	kev.password: null
      |	kev.url: https://www.cisa.gov/sites/default/files/feeds/known_exploited_vulnerabilities.json
      |	kev.user: null
      |	max.download.threads: 1
      |	nvd.api.check.validforhours: 4
      |	nvd.api.datafeed.bearertoken: null
      |	nvd.api.datafeed.password: null
      |	nvd.api.datafeed.startyear: null
      |	nvd.api.datafeed.url: null
      |	nvd.api.datafeed.user: null
      |	nvd.api.datafeed.validfordays: 7
      |	nvd.api.delay: 0
      |	nvd.api.endpoint: null
      |	nvd.api.key: null
      |	nvd.api.max.retry.count: 30
      |	nvd.api.results.per.page: null
      |	odc.analysis.timeout: 180
      |	odc.application.name: Dependency-Check Core
      |	odc.application.version: 12.1.3
      |	odc.autoupdate: true
      |	odc.ecosystem.maxquerylimit.: null
      |	odc.ecosystem.maxquerylimit.default: 100
      |	odc.maven.local.repo: null
      |	odc.reports.pretty.print: false
      |	odc.settings.mask: .*password.*,.*token.*,.*api.key.*
      |	proxy.disableSchemas: true
      |	proxy.nonproxyhosts: null
      |	proxy.password: null
      |	proxy.port: null
      |	proxy.server: null
      |	proxy.username: null
      |	suppression.file: null
      |	suppression.file.bearertoken: null
      |	suppression.file.password: null
      |	suppression.file.user: null
      |	temp.directory: null
      |	updater.nvdcve.enabled: true
      |	updater.versioncheck.enabled: true
      |	vfeed.connection_string: null
      |	vfeed.data_file: null
      |	vfeed.download_file: null
      |	vfeed.download_url: null
      |	vfeed.update_status: null
      |
      |""".stripMargin
}
