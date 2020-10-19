package de.upb.cs.uc4.hyperledger.testData

object TestDataCourses {
  def invalidCourseData(testCourseId: String): String = "{\"courseId\":\"" + testCourseId + "\",\"courseName\":\"IQC\",\"courseType\":\"Lecture\",\"startDate\":\"today\",\"endDate\":\"1999-01-01\",\"ects\":0,\"lecturerId\":\"Mustermann\",\"maxParticipants\":80,\"currentParticipants\":20,\"courseLanguage\":\"English\",\"courseDescription\":\"Fun new course\"}"

  def exampleCourseData(testCourseId: String): String = "{\"courseId\":\"" + testCourseId + "\",\"courseName\":\"IQC\",\"courseType\":\"Lecture\",\"startDate\":\"1998-01-01\",\"endDate\":\"1999-01-01\",\"ects\":7,\"lecturerId\":\"Mustermann\",\"maxParticipants\":80,\"currentParticipants\":20,\"courseLanguage\":\"English\",\"courseDescription\":\"Fun new course\"}"

  def exampleCourseData2(testCourseId: String): String = "{\"courseId\":\"" + testCourseId + "\",\"courseName\":\"Intro to Quantum\",\"courseType\":\"Lecture\",\"startDate\":\"1998-01-01\",\"endDate\":\"1999-01-01\",\"ects\":7,\"lecturerId\":\"Mustermann\",\"maxParticipants\":80,\"currentParticipants\":20,\"courseLanguage\":\"English\",\"courseDescription\":\"Fun new course\"}"
}
