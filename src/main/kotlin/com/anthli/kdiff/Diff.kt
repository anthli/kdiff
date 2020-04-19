/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
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