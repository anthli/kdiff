/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EditGraphTest {
  @Test
  fun `test get and set with valid coordinates`() {
    val editGraph = EditGraph(2, 2)
    editGraph[0, 0] = 1
    editGraph[0, 1] = 2
    editGraph[1, 0] = 3
    editGraph[1, 1] = 4

    Assertions.assertEquals(1, editGraph[0, 0])
    Assertions.assertEquals(2, editGraph[0, 1])
    Assertions.assertEquals(3, editGraph[1, 0])
    Assertions.assertEquals(4, editGraph[1, 1])
  }

  @Test
  fun `test get with out of bounds coordinates`() {
    val editGraph = EditGraph(1, 1)
    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[0, 2]
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[2, 0]
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[2, 2]
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[-1, 0]
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[0, -1]
    }
  }

  @Test
  fun `test get and set with out of bounds coordinates`() {
    val editGraph = EditGraph(1, 1)
    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[0, 2] = 1
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[2, 0] = 1
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[2, 2] = 1
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[-1, 0] = 1
    }

    Assertions.assertThrows(IndexOutOfBoundsException::class.java) {
      editGraph[0, -1] = 1
    }
  }
}