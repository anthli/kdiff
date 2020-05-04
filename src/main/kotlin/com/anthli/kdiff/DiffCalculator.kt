/**
 * Copyright (c) 2020 Anthony Li
 *
 * This source code is licensed under the MIT license (see LICENSE for details)
 */

package com.anthli.kdiff

import kotlin.math.max

/**
 * Diff implementation that takes in two sequences and generates a diff output.
 *
 * Optimizations are inspired by Neil Fraser's Diff Strategies found
 * [here](https://neil.fraser.name/writing/diff/).
 */
class DiffCalculator(private val oldString: String, private val newString: String) {
  /**
   * Computes the sequence of [Diff]s between the old and new strings.
   *
   * @return The sequence of [Diff]s between the old and new strings.
   */
  fun compute(): Sequence<Diff> {
    // Everything in the new string is inserted; there are no diffs to compute
    if (oldString.isEmpty()) {
      return sequenceOf(Diff(Operation.INSERT, newString))
    }

    // Everything in the old string is deleted; there are no diffs to compute
    if (newString.isEmpty()) {
      return sequenceOf(Diff(Operation.DELETE, oldString))
    }

    // Both strings are equal; there are no diffs to compute
    if (oldString == newString) {
      return sequenceOf(Diff(Operation.EQUAL, oldString))
    }

    // Compute the common affixes between both strings as a performance
    // optimization
    val commonPrefix = getCommonPrefix(oldString, newString)
    val commonSuffix = getCommonSuffix(oldString, newString)
    val commonPrefixLength = commonPrefix.length

    // Ignore the common suffix if the common prefix ends with it. This prevents
    // an affix from being classified as both a prefix and suffix. Otherwise,
    // what will happen is that the shorter string will throw a
    // StringIndexOutOfBoundsException and the substring of the longer string
    // will end up containing less letters than it should. For example,
    //
    // Old String: AABB
    // New String: AABBCDEFBB
    // Common Prefix: AABB
    // Common Suffix: BB
    //
    // The expected diff here would be "CDEFBB" being inserted to the right of
    // "AABB". Without this check, the optimized old string will throw an
    // exception. The optimized new string will be "CDEF" instead of "CDEFBB"
    // because it will take account of the common suffix in the substring
    // end index.
    val commonSuffixLength = if (commonPrefix.endsWith(commonSuffix)) {
      0
    }
    else {
      commonSuffix.length
    }

    val optimizedOldStringEndIndex = oldString.length - commonSuffixLength
    val optimizedNewStringEndIndex = newString.length - commonSuffixLength
    val optimizedOldString = oldString.substring(commonPrefixLength, optimizedOldStringEndIndex)
    val optimizedNewString = newString.substring(commonPrefixLength, optimizedNewStringEndIndex)
    val commonPrefixDiff = Diff(Operation.EQUAL, oldString.substring(0, commonPrefixLength))
    val commonSuffixDiff = Diff(Operation.EQUAL, oldString.substring(optimizedOldStringEndIndex))

    // Compute the diffs between both strings without any affixes
    val editGraph = getLcsEditGraph(optimizedOldString, optimizedNewString)
    val computedDiffs = computeDiffs(editGraph, optimizedOldString, optimizedNewString)
    return sequenceOf(commonPrefixDiff)
      .plus(computedDiffs)
      .plus(commonSuffixDiff)
  }

  /**
   * Finds and returns the common prefix between the old and new strings.
   *
   * @param oldString
   *   The old string to find the common prefix for.
   * @param newString
   *   The new string to find the common prefix for.
   */
  private fun getCommonPrefix(oldString: String, newString: String): String {
    val commonPrefixBuilder = StringBuilder()
    var i = 0
    while (i < oldString.length && i < newString.length) {
      // The moment characters don't match, the prefix ends
      if (oldString[i] != newString[i]) {
        break
      }

      commonPrefixBuilder.append(oldString[i])
      i++
    }

    return commonPrefixBuilder.toString()
  }

  /**
   * Finds and returns the common suffix between the old and new strings.
   *
   * @param oldString
   *   The old string to find the common suffix for.
   * @param newString
   *   The new string to find the common suffix for.
   */
  private fun getCommonSuffix(oldString: String, newString: String): String {
    val commonSuffixBuilder = StringBuilder()
    var i = oldString.length - 1
    var j = newString.length - 1
    while (i >= 0 && j >= 0) {
      // The moment characters don't match, the suffix ends
      if (oldString[i] != newString[j]) {
        break
      }

      commonSuffixBuilder.append(oldString[i])
      i--
      j--
    }

    // Reverse the builder since the common suffix letters were added backwards
    return commonSuffixBuilder.reverse().toString()
  }

  /**
   * Backtracks the [EditGraph] to produce the [Diff]s between the old and new
   * strings.
   *
   * @param editGraph
   *   The [EditGraph] containing the paths of the longest common subsequences.
   * @param oldString
   *   The old string to compute the [Diff]s for
   * @param newString
   *   The new string to compute the [Diff]s for.
   * @return The sequence of [Diff]s between the old and new strings.
   */
  private fun computeDiffs(
    editGraph: EditGraph,
    oldString: String,
    newString: String
  ): Sequence<Diff> {
    return computeDiffs(
      editGraph,
      oldString,
      newString,
      oldString.length,
      newString.length,
      emptySequence()
    )
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
   * @param oldString
   *   The old string to compute the [Diff]s for
   * @param newString
   *   The new string to compute the [Diff]s for.
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
    oldString: String,
    newString: String,
    m: Int,
    n: Int,
    acc: Sequence<Diff>
  ): Sequence<Diff> {
    // Moving up diagonally to the left on the edit graph indicates matching
    // characters in both strings.
    if (m > 0 && n > 0 && oldString[m - 1] == newString[n - 1]) {
      return computeDiffs(editGraph, oldString, newString, m - 1, n - 1, acc)
        .plus(Diff(Operation.EQUAL, oldString[m - 1].toString()))
    }

    // Moving up in the edit graph indicates a deletion of an old character from
    // the old string. Using > instead of >= here ensures that deletions will
    // occur before insertions because the inequality check is strict.
    if (m > 0 && (n == 0 || editGraph[m - 1, n] > editGraph[m, n - 1])) {
      return computeDiffs(editGraph, oldString, newString, m - 1, n, acc)
        .plus(Diff(Operation.DELETE, oldString[m - 1].toString()))
    }

    // Moving left in the edit graph indicates an insertion of a new character
    // from the new string. Using <= instead of < ensures that insertions will
    // occur after deletions because the inequality check is not strict.
    if (n > 0 && (m == 0 || editGraph[m - 1, n] <= editGraph[m, n - 1])) {
      return computeDiffs(editGraph, oldString, newString, m, n - 1, acc)
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
   * @param oldString
   *   The old string to construct the [EditGraph] for.
   * @param newString
   *   The new string to construct the [EditGraph] for.
   * @return
   *   The [EditGraph] containing the paths of the longest common
   *   subsequences.
   */
  private fun getLcsEditGraph(oldString: String, newString: String): EditGraph {
    val oldStringLength = oldString.length
    val newStringLength = newString.length
    val editGraph = EditGraph(oldStringLength, newStringLength)

    for (i in 1..oldStringLength) {
      for (j in 1..newStringLength) {
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