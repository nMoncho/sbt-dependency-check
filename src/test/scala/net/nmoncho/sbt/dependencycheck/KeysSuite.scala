/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck

import org.owasp.dependencycheck.reporting.ReportGenerator.{ Format => OwaspFormat }

/** Guards the [[Keys.Format]] re-export against a mis-mapped constant: each re-exported value must
  * point at the matching OWASP `ReportGenerator.Format` constant.
  */
class KeysSuite extends munit.FunSuite {

  test("Keys.Format re-exports every OWASP report format constant correctly") {
    assertEquals(Keys.Format.HTML, OwaspFormat.HTML)
    assertEquals(Keys.Format.XML, OwaspFormat.XML)
    assertEquals(Keys.Format.CSV, OwaspFormat.CSV)
    assertEquals(Keys.Format.JSON, OwaspFormat.JSON)
    assertEquals(Keys.Format.JUNIT, OwaspFormat.JUNIT)
    assertEquals(Keys.Format.SARIF, OwaspFormat.SARIF)
    assertEquals(Keys.Format.JENKINS, OwaspFormat.JENKINS)
    assertEquals(Keys.Format.GITLAB, OwaspFormat.GITLAB)
    assertEquals(Keys.Format.ALL, OwaspFormat.ALL)
  }
}
