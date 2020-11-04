package de.upb.cs.uc4.hyperledger.tests.testUtil

object TestDataExaminationRegulation {
  def validExaminationRegulation(name: String, modules: Array[String], state: Boolean): String =
    validExaminationRegulation(name, TestHelper.getJsonList(modules), if (state) "true" else "false")
  def validExaminationRegulation(name: String, modules: String, state: String): String =
    "{\"name\":\"" + name + ",\"active\":" + state + ",\"modules\":" + modules + "}"

  def getModule(id: String, name: String): String = {
    "{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}"
  }
}
