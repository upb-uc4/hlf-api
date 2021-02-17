package de.upb.cs.uc4.hyperledger.testData

import de.upb.cs.uc4.hyperledger.testUtil.TestHelperStrings

object TestDataExaminationRegulation {
  def validExaminationRegulation(name: String, modules: Seq[String], isOpen: Boolean): String = {
    val modulesString = TestHelperStrings.getJsonList(modules)
    "{\"name\":\"" + name + "\",\"active\":" + isOpen + ",\"modules\":" + modulesString + "}"
  }

  def getModule(id: String): String = getModule(id, id)
  def getModule(id: String, name: String): String = {
    "{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}"
  }
}
