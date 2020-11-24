package de.upb.cs.uc4.hyperledger.tests.testUtil

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

object TestDataMatriculation {
  def validMatriculationData1(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def validMatriculationData2(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"WS2019/20\"\n      ]\n    }\n  ]\n}"
  def validMatriculationData3(id: String): String = "{\"enrollmentId\":\"" + id + "\",\"matriculationStatus\":[{\"fieldOfStudy\":\"Media Sciences\",\"semesters\":[\"WS2020/21\"]},{\"fieldOfStudy\":\"Mathematics\",\"semesters\":[\"WS2020/21\",\"SS2018\"]}]}"
  def validMatriculationData4(id: String): String = "{\"enrollmentId\":\"" + id + "\",\"matriculationStatus\":[{\"fieldOfStudy\":\"Media Sciences\",\"semesters\":[\"WS2020/21\"]},{\"fieldOfStudy\":\"Mathematics\",\"semesters\":[\"WS2020/21\",\"SS2018\"]}]}"

  def invalidMatriculationJsonNoSemester(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n        }\n  ]\n}"
  def invalidMatriculationJsonNoFieldOfStudy(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonNoMatriculationStatus(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n}"
  def invalidMatriculationJsonNoMatriculationId: String = "{\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"

  def invalidMatriculationJsonInvalidId: String = "{\n  \"matriculationId\": \"" + "" + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData1(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"S2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData2(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS200\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData3(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"WW2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData4(id: String): String = "{\n  \"matriculationId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"2020\"\n      ]\n    }\n  ]\n}"

  def getSubjectMatriculationList(fieldOfStudy: String, semester: String): String = {
    "[{\"fieldOfStudy\":\"" + fieldOfStudy + "\", \"semesters\": [\"" + semester + "\"]}]"
  }
  def validMatriculationEntry: String = "[{\"fieldOfStudy\":\"Computer Science\",\"semesters\":[\"SS2022\"]}]"

  def establishExaminationRegulations(connection: ConnectionExaminationRegulationTrait): Unit = {
    try {
      val names = Seq("Computer Science", "Mathematics", "Media Sciences")
      for (name: String <- names) {
        establishExaminationRegulation(connection, name)
      }
    }
    catch {
      case e: Throwable => Logger.err("Error during estblishing Regulations: ", e)
    }
  }

  def establishExaminationRegulation(connection: ConnectionExaminationRegulationTrait, name: String): Unit = {
    val existingValue = connection.getExaminationRegulations(TestHelper.getJsonList(Array("\"" + name + "\"")))
    if (existingValue == "[]") {
      val examinationRegulation = TestDataExaminationRegulation.validExaminationRegulation(name, Array("M.1", "M.2"), true)
      connection.addExaminationRegulation(examinationRegulation)
    }
  }
}
