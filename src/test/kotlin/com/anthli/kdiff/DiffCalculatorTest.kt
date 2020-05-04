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
    val oldString = """
      AB
      C
    """.trimIndent()
    val newString = """
      A
      BC
    """.trimIndent()
    testDiff(oldString, newString, "A-B\n+BC")
  }

  @Test
  fun `diff with newline character inserted`() {
    val oldString = "ABC"
    val newString = """
      A
      BC
    """.trimIndent()
    testDiff(oldString, newString, "A+\nBC")
  }

  @Test
  fun `diff with newline character deleted`() {
    val oldString = """
      AB
      C
    """.trimIndent()
    val newString = "ABC"
    testDiff(oldString, newString, "AB-\nC")
  }

  /**
   * Prefixes are computed before suffixes, so the algorithm will detect that
   * the second "A" is inserted.
   *
   * This also tests the affix computation where the common prefix contains at
   * least part of the common suffix.
   */
  @Test
  fun `diff between "A" and "AA" expects second "A" to be inserted`() {
    testDiff("A", "AA", "A+A")
  }

  /**
   * Prefixes are computed before suffixes, so the algorithm will detect that
   * the second "A" is deleted.
   *
   * This tests the affix computation where there is a common prefix between the
   * old and new strings. However, part of the common prefix is also a part of
   * the common suffix. This causes a StringIndexOutOfBoundsException since the
   * indices overlap
   */
  @Test
  fun `diff between "AA" and "A" expects second "A" to be deleted`() {
    testDiff("AA", "A", "A-A")
  }

  /**
   * This tests the affix computation where there is a common prefix between the
   * old and new strings that also ends with the common suffix.
   */
  @Test
  fun `diff between strings where common prefix contains common suffix`() {
    testDiff("AAB", "AABB", "AAB+B")
    testDiff("AABB", "AAB", "AAB-B")
    testDiff("AABB", "AABBCDEFBB", "AABB+C+D+E+F+B+B")
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