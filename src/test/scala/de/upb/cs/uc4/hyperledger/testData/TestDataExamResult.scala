package de.upb.cs.uc4.hyperledger.testData

import de.upb.cs.uc4.hyperledger.testUtil.TestHelperStrings

object TestDataExamResult {
  def customizableExamResult(listOfResults: Seq[String]): String = {
    val entryListJson: String = TestHelperStrings.getJsonList(listOfResults)
    s"""{
      |"examResultEntries": $entryListJson
      |}
      |
      |""".stripMargin
  }

  def customizableExamResultEntry(enrollmentId: String, examId: String, grade: String): String = {
    s"""{
      |  "enrollmentId": "$enrollmentId",
      |  "examId": "$examId",
      |  "grade": "$grade"
      |}
      |""".stripMargin
  }
}
