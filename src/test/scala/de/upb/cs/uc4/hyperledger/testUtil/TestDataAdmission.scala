package de.upb.cs.uc4.hyperledger.testUtil

object TestDataAdmission {
  def courseAdmission1(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, "C.1", "Admission_Module_1", "2020-12-31T23:59:59")
  def courseAdmission2(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, "C.2", "Admission_Module_3", "2020-12-31T23:59:59")
  def courseAdmission_noAdmissionId(studentId: String): String = TestDataAdmission.validCourseAdmissionNoAdmissionId(studentId, "C.2", "Admission_Module_1", "2020-12-31T23:59:59")
  def courseAdmission_noAdmissionId_WithId(studentId: String): String = TestDataAdmission.validCourseAdmission(studentId, "C.2", "Admission_Module_1", "2020-12-31T23:59:59")

  def validCourseAdmission(student: String, course: String, module: String, timestamp: String): String = {
    s"""{
       |  "admissionId": "$student:$course",
       |  "enrollmentId": "$student",
       |  "courseId": "$course",
       |  "moduleId": "$module",
       |  "timestamp": "$timestamp",
       |  "type": "Course"
       |}
       |""".stripMargin
  }
  def validCourseAdmissionNoAdmissionId(student: String, course: String, module: String, timestamp: String): String = {
    "{\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\",\"type\":\"Course\"}"
  }

  def validExamAdmission(enrollmentId: String, examId: String): String =
    customizableExamAdmission(enrollmentId, examId, TestHelperStrings.getCurrentDate)
  def customizableExamAdmission(enrollmentId: String, examId: String, timestamp: String = "", admissionType: String = "Exam"): String =
    fullyCustomizableExamAdmission(s"$enrollmentId:$examId", enrollmentId, examId, timestamp, admissionType)
  def fullyCustomizableExamAdmission(admissionId: String, enrollmentId: String, examId: String, timestamp: String = "", admissionType: String = "Exam"): String = {
    s"""{
       |  "examId": "$examId",
       |  "admissionId": "$admissionId",
       |  "enrollmentId": "$enrollmentId",
       |  "timestamp": "$timestamp",
       |  "type": "$admissionType"
       |}
       |""".stripMargin
  }
}
