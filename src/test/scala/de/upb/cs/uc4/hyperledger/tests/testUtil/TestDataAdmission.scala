package de.upb.cs.uc4.hyperledger.tests.testUtil

object TestDataAdmission {
  val admission1: String = TestDataAdmission.validAdmission("AdmissionStudent_1", "C.1", "AdmissionModule_1", "2020-12-31T23:59:59")
  val admission2: String = TestDataAdmission.validAdmission("AdmissionStudent_2", "C.2", "AdmissionModule_3", "2020-12-31T23:59:59")
  val admission_noAdmissionId: String = TestDataAdmission.validAdmissionNoAdmissionId("AdmissionStudent_1", "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")
  val admission_noAdmissionId_WithId: String = TestDataAdmission.validAdmission("AdmissionStudent_1", "C.2", "AdmissionModule_1", "2020-12-31T23:59:59")

  def validAdmission(student: String, course: String, module: String, timestamp: String): String = {
    "{\"admissionId\":\"" + student + ":" + course + "\",\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
  def validAdmissionNoAdmissionId(student: String, course: String, module: String, timestamp: String): String = {
    "{\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
}
