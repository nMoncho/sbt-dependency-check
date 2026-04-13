/*
 * Copyright 2025 the original author or authors
 *
 * SPDX-License-Identifier: MIT
 */

package net.nmoncho.sbt.dependencycheck.settings

import net.nmoncho.sbt.dependencycheck.settings.SuppressionRule.PropertyType

class PropertyTypeSyntaxSuite extends munit.FunSuite {

  test("Create Property with extension methods from a String") {
    import SuppressionRule.PropertyType.*

    val cve = "CVE-12345".toPropertyType()
    assertEquals(cve.value, "CVE-12345")
    assert(!cve.regex)
    assert(!cve.caseSensitive)

    val cveSensitive = "Cve-12345".toPropertyType(caseSensitive = true)
    assertEquals(cveSensitive.value, "Cve-12345")
    assert(!cveSensitive.regex)
    assert(cveSensitive.caseSensitive)
  }

  test("Create Property with extension methods from a Regex") {
    import SuppressionRule.PropertyType.*

    val cveInsensitive = "CVE-12345".r.toPropertyType
    assertEquals(cveInsensitive.value, "CVE-12345")
    assert(cveInsensitive.regex)
    assert(cveInsensitive.caseSensitive)

    val cveSensitive = "(?i)Cve-12345".r.toPropertyType
    assertEquals(cveSensitive.value, "(?i)Cve-12345")
    assert(cveSensitive.regex)
    assertEquals(cveSensitive.caseSensitive, false)
  }

  test("Create Property with conversion methods") {
    import SuppressionRule.PropertyType.PropertyTypeImplicitConversions.*

    val cve: PropertyType = "CVE-12345"
    assertEquals(cve.value, "CVE-12345")
    assert(!cve.regex)
    assert(cve.caseSensitive)

    val cveRegex: PropertyType = "CVE-12345".r
    assertEquals(cveRegex.value, "CVE-12345")
    assert(cveRegex.regex)
    assert(cveRegex.caseSensitive)
  }
}
