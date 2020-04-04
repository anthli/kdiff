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