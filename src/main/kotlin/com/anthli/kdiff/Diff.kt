/**
 * kdiff - a diff library in Kotlin
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

import kotlinx.serialization.Serializable

/**
 * Encapsulation of the type of operation and the involved text of a diff.
 */
@Serializable
data class Diff(val op: Operation, val text: String) {
  override fun toString(): String {
    return buildString {
      append(when (op) {
        Operation.INSERT -> "+"
        Operation.DELETE -> "-"
        Operation.EQUAL -> ""
      })
      append(text)
    }
  }

  override fun equals(other: Any?): Boolean {
    return other is Diff
      && op == other.op
      && text == other.text
  }
}