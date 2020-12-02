package de.upb.cs.uc4.hyperledger.tests.testUtil

object TestDataAdmission {
  def validAdmission(student: String, course: String, module: String, timestamp: String): String = {
    "{\"admissionId\":\"" + student+":"+course + "\",\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
  def validAdmissionNoAdmissionId(student: String, course: String, module: String, timestamp: String): String = {
    "{\"enrollmentId\":\"" + student + "\",\"courseId\":\"" + course + "\",\"moduleId\":\"" + module + "\",\"timestamp\":\"" + timestamp + "\"}"
  }
}
