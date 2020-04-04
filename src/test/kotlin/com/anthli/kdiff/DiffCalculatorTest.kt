/**
 * kdiff - diff library implemented in Kotlin.
 * Copyright (C) 2020 Anthony Li
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    val a = """
      ABC
    """.trimIndent()
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
    val b = """
      ABC
    """.trimIndent()
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