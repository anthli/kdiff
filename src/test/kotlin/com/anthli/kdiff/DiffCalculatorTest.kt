/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DiffCalculatorTest {
  @Test
  fun `diffs generated for "ABCABBA" and "CBABAC"`() {
    val a = "ABCABBA"
    val b = "CBABAC"
    testDiff(a, b, "-A-BC-AB+ABA+C")
  }

  @Test
  fun `diffs generated for "!@#T$%^" and "&*T()-"`() {
    val a = "!@#T$%^"
    val b = "&*T()-"
    testDiff(a, b, "-!-@-#+&+*T-$-%-^+(+)+-")
  }

  @Test
  fun `diff generated for "A" and "B"`() {
    val a = "A"
    val b = "B"
    testDiff(a, b, "-A+B")
  }

  @Test
  fun `equal diff generated for "A" and "A"`() {
    val a = "A"
    val b = "A"
    testDiff(a, b, "A")
  }

  @Test
  fun `all insert diffs generated for "" and "ABC"`() {
    val a = ""
    val b = "ABC"
    testDiff(a, b, "+A+B+C")
  }

  @Test
  fun `all delete diffs generated for "ABC" and ""`() {
    val a = "ABC"
    val b = ""
    testDiff(a, b, "-A-B-C")
  }

  @Test
  fun `diff with newline character`() {
    val a = """
      AB
      C
    """.trimIndent()
    val b = """
      A
      BC
    """.trimIndent()
    testDiff(a, b, "A-B\n+BC")
  }

  @Test
  fun `diff with newline character inserted`() {
    val a = "ABC"
    val b = """
      A
      BC
    """.trimIndent()
    testDiff(a, b, "A+\nBC")
  }

  @Test
  fun `diff with newline character deleted`() {
    val a = """
      AB
      C
    """.trimIndent()
    val b = "ABC"
    testDiff(a, b, "AB-\nC")
  }

  private fun testDiff(a: String, b: String, expectedDiffString: String) {
    val diffCalculator = DiffCalculator(a, b)
    val actualDiffs = diffCalculator.compute().toList()
    Assertions.assertTrue(actualDiffs.count() > 0)

    val actualDiffString = actualDiffs.joinToString("")
    Assertions.assertEquals(expectedDiffString, actualDiffString)
  }
}