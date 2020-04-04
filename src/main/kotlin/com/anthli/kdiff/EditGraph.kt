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