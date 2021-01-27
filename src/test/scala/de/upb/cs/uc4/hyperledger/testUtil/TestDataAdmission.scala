package de.upb.cs.uc4.hyperledger.testUtil

object TestDataAdmission {
  def admission1(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.1", "AdmissionModule_1", "2020-12-31T23:59:59")
  def admission2(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.2", "AdmissionModule_3", "2020-12-31T23:59:59")
  def admission_noAdmissionId(studentId: String): String = TestDataAdmission.validAdmissionNoAdmissionId(studentId, "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")
  def admission_noAdmissionId_WithId(studentId: String): String = TestDataAdmission.validAdmission(studentId, "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")

  def validAdmission(student: String, course: String, module: String, timestamp: String): String = {
    "{\"admissionId\":\"" + student + ":" + course + "\",\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
  def validAdmissionNoAdmissionId(student: String, course: String, module: String, timestamp: String): String = {
    "{\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
}
