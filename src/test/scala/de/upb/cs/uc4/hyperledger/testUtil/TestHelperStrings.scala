package de.upb.cs.uc4.hyperledger.testUtil

import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object TestHelperStrings {
  // JSON
  def getJsonList(items: Seq[String]): String = {
    "[" + nullableSeqToString(items) + "]"
  }
  def compareJson(expected: String, actual: String): Assertion = {
    val cleanExpected = removeNewLinesAndSpaces(expected)
    val cleanActual = removeNewLinesAndSpaces(actual)
    cleanActual should be(cleanExpected)
  }
  def nullableSeqToString(input: Seq[String]): String = {
    if (input == null) ""
    else input.mkString(", ")
  }

  // ADJUSTMENT
  def removeNewLinesAndSpaces(input: String): String = {
    removeSpaces(removeNewLines(input))
  }
  def normalizeLineEnds(item: String): String = {
    item
      .replace("\\r\\n", "\n")
      .replace("\\r\n", "\n")
      .replace("\r\\n", "\n")
      .replace("\r\n", "\n")
      .replace("\\r", "\n")
      .replace("\r", "\n")
  }
  def removeSpaces(item: String): String = {
    item
      .replace(" ", "")
  }
  def removeNewLines(item: String): String = {
    normalizeLineEnds(item)
      .replace("\\n", "")
      .replace("\n", "")
  }
}
