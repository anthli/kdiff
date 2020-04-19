/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@UnstableDefault
class DiffTest {
  @Test
  fun `test Operation INSERT toString`() {
    val diff = Diff(Operation.INSERT, "A")
    Assertions.assertEquals("+A", diff.toString())
  }

  @Test
  fun `test Operation DELETE toString`() {
    val diff = Diff(Operation.DELETE, "b")
    Assertions.assertEquals("-b", diff.toString())
  }

  @Test
  fun `test Operation EQUAL toString`() {
    val diff = Diff(Operation.EQUAL, "1")
    Assertions.assertEquals("1", diff.toString())
  }

  @Test
  fun `test JSON stringify`() {
    val diff = Diff(Operation.INSERT, "A")
    val json = Json.stringify(Diff.serializer(), diff)
    Assertions.assertEquals("""{"op":"INSERT","text":"A"}""", json)
  }

  @Test
  fun `test JSON parse`() {
    val expectedDiff = Diff(Operation.DELETE, "B")
    val actualDiff = Json.parse(Diff.serializer(), """{"op":"DELETE","text":"B"}""")
    Assertions.assertEquals(expectedDiff, actualDiff)
  }
}