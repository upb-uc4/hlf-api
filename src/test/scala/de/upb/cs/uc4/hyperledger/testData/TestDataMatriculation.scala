package de.upb.cs.uc4.hyperledger.testData

object TestDataMatriculation {
  def validMatriculationData1(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def validMatriculationData2(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"WS2019/20\"\n      ]\n    }\n  ]\n}"
  def validMatriculationData3(id: String): String = "{\"enrollmentId\":\"" + id + "\",\"matriculationStatus\":[{\"fieldOfStudy\":\"Media Sciences\",\"semesters\":[\"WS2020/21\"]},{\"fieldOfStudy\":\"Mathematics\",\"semesters\":[\"WS2020/21\",\"SS2018\"]}]}"
  def validMatriculationData4(id: String): String = "{\"enrollmentId\":\"" + id + "\",\"matriculationStatus\":[{\"fieldOfStudy\":\"Media Sciences\",\"semesters\":[\"WS2020/21\"]},{\"fieldOfStudy\":\"Mathematics\",\"semesters\":[\"WS2020/21\",\"SS2018\"]}]}"
  def validMatriculationDataCustom(id: String, examinationRegulation: String): String =
    validMatriculationDataCustom_MultipleExamRegs(id, Seq(examinationRegulation))
  def validMatriculationDataCustom_MultipleExamRegs(id: String, examinationRegulations: Seq[String]): String =
    "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    " +
      examinationRegulations.map(examReg => validMatriculationListEntry(examReg)).mkString(",") + "  ]\n}"

  def invalidMatriculationJsonNoSemester(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n        }\n  ]\n}"
  def invalidMatriculationJsonNoFieldOfStudy(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonNoMatriculationStatus(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n}"
  def invalidMatriculationJsonNoEnrollmentId: String = "{\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"

  def invalidMatriculationJsonInvalidId: String = "{\n  \"enrollmentId\": \"" + "" + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData1(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"S2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData2(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"SS200\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData3(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"WW2020\"\n      ]\n    }\n  ]\n}"
  def invalidMatriculationJsonInvalidMatriculationData4(id: String): String = "{\n  \"enrollmentId\": \"" + id + "\",\n  \"matriculationStatus\": [\n    {\n      \"fieldOfStudy\": \"Computer Science\",\n      \"semesters\": [\n        \"2020\"\n      ]\n    }\n  ]\n}"

  def getSubjectMatriculationList(fieldOfStudy: String, semester: String): String = {
    "[{\"fieldOfStudy\":\"" + fieldOfStudy + "\", \"semesters\": [\"" + semester + "\"]}]"
  }
  def validMatriculationEntry: String = s"[${validMatriculationListEntry("Computer Science")}]"
  def validMatriculationListEntry(fosName: String): String = s"{\"fieldOfStudy\":\"$fosName\",\"semesters\":[\"SS2022\"]}"

  def testModule(id: String): String = TestDataExaminationRegulation.getModule(id, id)
}
