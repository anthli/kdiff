/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

/**
 * Implementation of an edit graph used for tabulating the Longest Common
 * Subsequence algorithm.
 *
 * An edit graph is an m x n matrix that holds the point values for each path
 * taken when finding a solution for the longest common subsequence.
 */
internal class EditGraph(m: Int, n: Int) {
  private val editGraph = Array(m + 1) { IntArray(n + 1) }

  operator fun get(x: Int, y: Int): Int {
    return editGraph[x][y]
  }

  operator fun set(x: Int, y: Int, value: Int) {
    editGraph[x][y] = value
  }

  /**
   * @return A comma-delimited representation of the edit graph.
   */
  override fun toString(): String {
    return buildString {
      editGraph.forEach { row ->
        row.forEachIndexed { j, col ->
          append(col)

          if (j < row.size - 1) {
            append(", ")
          }
          else {
            append("\n")
          }
        }
      }
    }
  }
}