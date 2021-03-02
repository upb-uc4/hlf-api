package de.upb.cs.uc4.hyperledger.testData

import java.text.SimpleDateFormat
import java.util.Calendar

object TestDataExam {
  def validFutureExam(courseId: String, lecturerId: String, moduleId: String, examType: String, ects: Int): String = {
    val current = Calendar.getInstance()
    current.add(Calendar.MINUTE, 10)
    val validAdmittableUntil = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(current.getTime)
    current.add(Calendar.SECOND, 1)
    val validDroppableUntil = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(current.getTime)
    current.add(Calendar.SECOND, 1)
    val validDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(current.getTime)
    validExam(courseId, lecturerId, moduleId, examType, validDate, ects, validAdmittableUntil, validDroppableUntil)
  }

  def validExam(courseId: String, lecturerId: String, moduleId: String, examType: String,
      date: String, ects: Int, admittableUntil: String, droppableUntil: String): String = {
    customizableExam(s"$courseId:$moduleId:$examType:$date", courseId, lecturerId, moduleId, examType, date, ects, admittableUntil, droppableUntil)
  }

  def invalidExamId(courseId: String, lecturerId: String, moduleId: String, examType: String,
      date: String, ects: Int, admittableUntil: String, droppableUntil: String): String = {
    customizableExam("garbage", courseId, lecturerId, moduleId, examType, date, ects, admittableUntil, droppableUntil)
  }

  def customizableExam(examId: String, courseId: String, lecturerId: String, moduleId: String, examType: String = "Written Exam",
      date: String = "2021-02-12T10:00:00.000Z", ects: Int = 6,
      admittableUntil: String = "2021-01-12T23:59:59.999Z",
      droppableUntil: String = "2021-02-05T23:59:59.999Z"): String = {
    s"""{
       |  "examId": "$examId",
       |  "courseId": "$courseId",
       |  "lecturerEnrollmentId": "$lecturerId",
       |  "moduleId": "$moduleId",
       |  "type": "$examType",
       |  "date": "$date",
       |  "ects": $ects,
       |  "admittableUntil": "$admittableUntil",
       |  "droppableUntil": "$droppableUntil"
       |}
       |""".stripMargin
  }

  def calculateExamId(examJson: String): String = {
    val courseIdWithStrings = getInfoFromExam(examJson, "courseId\":")
    val moduleIdWithStrings = getInfoFromExam(examJson, "moduleId\":")
    val examTypeWithStrings = getInfoFromExam(examJson, "type\":")
    val dateWithStrings = getInfoFromExam(examJson, "date\":")
    val courseId = stripFromStrings(courseIdWithStrings)
    val moduleId = stripFromStrings(moduleIdWithStrings)
    val examType = stripFromStrings(examTypeWithStrings)
    val date = stripFromStrings(dateWithStrings)
    val examId = s"$courseId:$moduleId:$examType:$date"
    println(s"EXAMID: $examId")

    examId
  }

  def stripFromStrings(info: String): String = {
    val stripFirst: String = info.split("\"").tail.head
    val stripEnd: String = stripFirst.split("\"").head
    stripEnd
  }

  def getInfoFromExam(examJson: String, identifier: String): String = {
    val stripFirst: String = examJson.split(identifier).tail.head
    if (stripFirst.contains(",")) {
      return stripFirst.split(",").head
    }
    return stripFirst.split("}").head
  }
}
