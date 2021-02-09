package de.upb.cs.uc4.hyperledger.testUtil

object TestDataAdmission {
  def admission1(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.1", "Admission_Module_1", "2020-12-31T23:59:59")
  def admission2(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.2", "Admission_Module_3", "2020-12-31T23:59:59")
  def admission_noAdmissionId(studentId: String): String = TestDataAdmission.validAdmissionNoAdmissionId(studentId, "C.2", "Admission_Module_1", "2020-12-31T23:59:59")
  def admission_noAdmissionId_WithId(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.2", "Admission_Module_1", "2020-12-31T23:59:59")

  def validAdmission(student: String, course: String, module: String, timestamp: String): String = {
    "{\"admissionId\":\"" + student + ":" + course + "\",\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
  def validAdmissionNoAdmissionId(student: String, course: String, module: String, timestamp: String): String = {
    "{\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }

  def validExamAdmission(enrollmentId: String, examId: String): String=
    customizableExamAdmission(enrollmentId, examId, TestHelperStrings.getCurrentDate, "Exam")
  def customizableExamAdmission(enrollmentId: String, examId: String, timestamp: String = "", admissionType: String = "Exam"): String =
    fullyCustomizableExamAdmission(s"$enrollmentId:$examId", enrollmentId, examId, timestamp, admissionType)
  def fullyCustomizableExamAdmission(admissionId: String, enrollmentId: String, examId: String, timestamp: String = "", admissionType: String = "Exam"): String = {
    s"""{
       |  "admissionId": "$admissionId",
       |  "enrollmentId": "$enrollmentId",
       |  "examId": "$examId",
       |  "timestamp": "$timestamp",
       |  "type": "$admissionType"
       |}
       |""".stripMargin
  }
}
