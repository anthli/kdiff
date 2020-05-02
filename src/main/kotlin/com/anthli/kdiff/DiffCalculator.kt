/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

import kotlin.math.max

/**
 * Diff implementation that takes in two sequences and generates a diff output.
 */
class DiffCalculator(
  private val oldString: String,
  private val newString: String
) {
  /**
   * Computes the sequence of [Diff]s between the old and new strings.
   *
   * @return The sequence of [Diff]s between the old and new strings.
   */
  fun compute(): Sequence<Diff> {
    val m = oldString.length
    val n = newString.length
    
    
    val editGraph = getLcsEditGraph(m, n)
    return computeDiffs(editGraph, m, n)
  }

  /**
   * Backtracks the [EditGraph] to produce the [Diff]s between the old and new
   * strings.
   *
   * @param editGraph
   *   The [EditGraph] containing the paths of the longest common subsequences.
   * @param m
   *   The length of the old string.
   * @param n
   *   The length of the new string.
   * @return The sequence of [Diff]s between the old and new strings.
   */
  private fun computeDiffs(
    editGraph: EditGraph,
    m: Int,
    n: Int
  ): Sequence<Diff> {
    return computeDiffs(editGraph, m, n, emptySequence())
  }

  /**
   * Backtracks the [EditGraph] to produce the [Diff]s between the old and new
   * strings.
   * This is a recursive helper function for accumulating all generated [Diff]s.
   *
   * Deletions have higher priority than insertions. For example, take strings
   * a = ABCABBA and b = CBABAC. The edit graph would be:
   * ```
   *     b |     C   B   A   B   A   C
   *     n | 0   1   2   3   4   5   6
   * a m   |
   * ------+--------------------------
   *   0   | 0   0   0   0   0   0   0
   *       |
   * A 1   | 0   0   0   1   1   1   1
   *       |
   * B 2   | 0   0   1   1   2   2   2
   *       |
   * C 3   | 0   1   1   1   2   2   3
   *       |
   * A 4   | 0   1   1   2   2   3   3
   *       |
   * B 5   | 0   1   2   2   3   3   3
   *       |
   * B 6   | 0   1   2   2   3   3   3
   *       |
   * A 7   | 0   1   2   3   3   4   4
   * ```
   *
   * The backtrace will be (* indicates character in LCS):
   * ```
   *     b |     C   B   A   B   A   C
   *     n | 0   1   2   3   4   5   6
   * a m   |
   * ------+--------------------------
   *   0   | 0   0   0   0   0   0   0
   *       | |
   * A 1   | 0   0   0   1   1   1   1
   *       | |
   * B 2   | 0   0   1   1   2   2   2
   *       |   \
   * C 3   | 0   1*  1   1   2   2   3
   *       |     |
   * A 4   | 0   1   1   2   2   3   3
   *       |       \
   * B 5   | 0   1   2*- 2   3   3   3
   *       |               \
   * B 6   | 0   1   2   2   3*  3   3
   *       |                   \
   * A 7   | 0   1   2   3   3   4*- 4
   * ```
   *
   * where the sequence of diffs would be:
   *
   * ```
   * A B C A B A B A C
   * - -   -   +     +
   * ```
   *
   * @param editGraph
   *   The [EditGraph] containing the paths of the longest common subsequences.
   * @param m
   *   The length of the old string.
   * @param n
   *   The length of the new string.
   * @param acc
   *   The accumulating sequence of [Diff]s.
   * @return
   *   The accumulated sequence of [Diff]s.
   */
  private fun computeDiffs(
    editGraph: EditGraph,
    m: Int,
    n: Int,
    acc: Sequence<Diff>
  ): Sequence<Diff> {
    // Moving up diagonally to the left on the edit graph indicates matching
    // characters in both strings.
    if (m > 0 && n > 0 && oldString[m - 1] == newString[n - 1]) {
      return computeDiffs(editGraph, m - 1, n - 1, acc)
        .plus(Diff(Operation.EQUAL, oldString[m - 1].toString()))
    }

    // Moving up in the edit graph indicates a deletion of an old character from
    // the old string. Using > instead of >= here ensures that deletions will
    // occur before insertions because the inequality check is strict.
    if (m > 0 && (n == 0 || editGraph[m - 1, n] > editGraph[m, n - 1])) {
      return computeDiffs(editGraph, m - 1, n, acc)
        .plus(Diff(Operation.DELETE, oldString[m - 1].toString()))
    }

    // Moving left in the edit graph indicates an insertion of a new character
    // from the new string. Using <= instead of < ensures that insertions will
    // occur after deletions because the inequality check is not strict.
    if (n > 0 && (m == 0 || editGraph[m - 1, n] <= editGraph[m, n - 1])) {
      return computeDiffs(editGraph, m, n - 1, acc)
        .plus(Diff(Operation.INSERT, newString[n - 1].toString()))
    }

    return acc
  }

  /**
   * An implementation of the Longest Common Subsequence algorithm using dynamic
   * programming via tabulation.
   *
   * This approach has a much better runtime of only O(MN) where M and N are the
   * lengths of the old and new strings, respectively.
   *
   * The result of this function is an [EditGraph] between the old and new
   * strings.
   *
   * Take an example with strings a = ABCABBA and b = CBABAC. The edit graph
   * would be:
   * ```
   *     b |     C   B   A   B   A   C
   *     n | 0   1   2   3   4   5   6
   * a m   |
   * ------+--------------------------
   *   0   | 0   0   0   0   0   0   0
   *       |
   * A 1   | 0   0   0   1   1   1   1
   *       |
   * B 2   | 0   0   1   1   2   2   2
   *       |
   * C 3   | 0   1   1   1   2   2   3
   *       |
   * A 4   | 0   1   1   2   2   3   3
   *       |
   * B 5   | 0   1   2   2   3   3   3
   *       |
   * B 6   | 0   1   2   2   3   3   3
   *       |
   * A 7   | 0   1   2   3   3   4   4
   * ```
   *
   * @param m
   *   The length of the old string.
   * @param n
   *   The length of the new string.
   * @return
   *   The [EditGraph] containing the paths of the longest common
   *   subsequences.
   */
  private fun getLcsEditGraph(m: Int, n: Int): EditGraph {
    val editGraph = EditGraph(m, n)

    for (i in 1..m) {
      for (j in 1..n) {
        if (oldString[i - 1] == newString[j - 1]) {
          editGraph[i, j] = 1 + editGraph[i - 1, j - 1]
        }
        else {
          editGraph[i, j] = max(editGraph[i - 1, j], editGraph[i, j - 1])
        }
      }
    }

    return editGraph
  }

  /**
   * Backtracks the [EditGraph] and produces the longest common subsequence.
   * The LCS is based on the first matching character in the old string.
   *
   * This will be kept here for educational purposes.
   *
   * @param editGraph
   *   The [EditGraph] containing the paths of the longest common subsequences.
   * @param m
   *   The length of the old string.
   * @param n
   *   The length of the new string.
   * @return
   *   The longest common subsequence between the old and new strings.
   */
  private fun getLcs(editGraph: EditGraph, m: Int, n: Int): String {
    if (m == 0 || n == 0) {
      return ""
    }

    if (oldString[m - 1] == newString[n - 1]) {
      return getLcs(editGraph, m - 1, n - 1) + oldString[m - 1]
    }

    if (editGraph[m - 1, n] < editGraph[m, n - 1]) {
      return getLcs(editGraph, m, n - 1)
    }

    return getLcs(editGraph, m - 1, n)
  }

  /**
   * A naive implementation of the Longest Common Subsequence algorithm.
   *
   * The issue with this implementation is that is suffers from overlapping
   * subproblems, i.e., the same problem is solved multiple times in the
   * recursive tree. This ends up with a runtime of O(2^N) where N is the sum of
   * the lengths of the old and new strings.
   *
   * This will be kept here for educational purposes.
   *
   * @param m
   *   The length of the old string.
   * @param n
   *   The length of the new string.
   */
  private fun naiveLcs(m: Int, n: Int): Int {
    if (m == 0 || n == 0) {
      return 0
    }

    if (oldString[m - 1] == newString[n - 1]) {
      return 1 + naiveLcs(m - 1, n - 1)
    }

    return max(naiveLcs(m - 1, n), naiveLcs(m, n -1))
  }
}