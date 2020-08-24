package de.upb.cs.uc4.hyperledger.testData

object TestDataMatriculation {
  def matriculationData(testStudentId: String): String = "{\n  \"matriculationId\": \"" + testStudentId + "\",\n  \"firstName\": \"Dieter\",\n  \"lastName\": \"Dietrich\",\n  \"birthDate\": \"2020-08-12\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def illegalMatriculationData: String = "{\n  \"matriculationId\": \"" + "123" + "\",\n  \"firstName\": \"Dieter\",\n  \"lastName\": \"Dietrich\",\n  \"birthDate\": \"2020-08-12\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def illegalMatriculationData2(testStudentId: String): String = "{\n  \"matriculationId\": \"" + testStudentId + "\",\n  \"firstName\": \"Dieter\",\n  \"lastName\": \"Dietrich\",\n  \"birthDate\": \"2020-08-12\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS220\"\n      ]\n    }\n  ]\n}"
}
