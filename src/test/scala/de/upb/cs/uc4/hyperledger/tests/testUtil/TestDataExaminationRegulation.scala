package de.upb.cs.uc4.hyperledger.tests.testUtil

object TestDataExaminationRegulation {
  def validExaminationRegulation(name: String, modules: Seq[String], state: Boolean): String = {
    val modulesString = TestHelper.getJsonList(modules)
    "{\"name\":\"" + name + "\",\"active\":" + state + ",\"modules\":" + modulesString + "}"
  }

  def getModule(id: String, name: String): String = {
    "{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}"
  }
}
