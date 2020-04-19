/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

/**
 * The different types of operations that can happen in a diff.
 */
enum class Operation {
  /**
   * A change caused by insertion.
   */
  INSERT,

  /**
   * A change caused by deletion.
   */
  DELETE,

  /**
   * No change.
   */
  EQUAL
}