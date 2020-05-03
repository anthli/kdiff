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
  fun `insert diff generated for empty old string`() {
    testDiff("", "A", "+A")
  }

  @Test
  fun `delete diff generated for empty new string`() {
    testDiff("A", "", "-A")
  }

  @Test
  fun `equal diff generated for "A" and "A"`() {
    testDiff("A", "A", "A")
  }

  @Test
  fun `diffs generated for same letter different case`() {
    testDiff("a", "A", "-a+A")
  }

  @Test
  fun `diffs generated for "ABCABBA" and "CBABAC"`() {
    testDiff("ABCABBA", "CBABAC", "-A-BC-AB+ABA+C")
  }

  @Test
  fun `diffs generated for "!@#T$%^" and "&*T()-"`() {
    testDiff("!@#T$%^", "&*T()-", "-!-@-#+&+*T-$-%-^+(+)+-")
  }

  @Test
  fun `diffs generated for "A" and "B"`() {
    testDiff("A", "B", "-A+B")
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

  /**
   * Prefixes are computed before suffixes, so the algorithm will detect that
   * the second "A" is inserted
   */
  @Test
  fun `diff between "A" and "AA" expects second "A" to be inserted`() {
    testDiff("A", "AA", "A+A")
  }

  /**
   * Prefixes are computed before suffixes, so the algorithm will detect that
   * the second "A" is deleted
   */
  @Test
  fun `diff between "AA" and "A" expects second "A" to be deleted`() {
    testDiff("AA", "A", "A-A")
  }

  private fun testDiff(
    oldString: String,
    newString: String,
    expectedDiffString: String
  ) {
    val diffCalculator = DiffCalculator(oldString, newString)
    val actualDiffs = diffCalculator.compute().toList()
    Assertions.assertTrue(actualDiffs.count() > 0)

    val actualDiffString = actualDiffs.joinToString("")
    Assertions.assertEquals(expectedDiffString, actualDiffString)
  }
}