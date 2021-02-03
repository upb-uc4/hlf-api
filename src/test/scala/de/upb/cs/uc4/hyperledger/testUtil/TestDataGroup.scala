package de.upb.cs.uc4.hyperledger.testUtil

object TestDataGroup {
  val adminGroupName = "Admin"
  val systemGroupName = "System"
  val lecturerGroupName = "Lecturer"

  val userList1: String = TestHelperStrings.getJsonList(Seq[String]("100", "101").map(s => {
    "\"" + s + "\""
  }))
  val group1: String = TestDataGroup.validGroup("someGroup", userList1)

  def validGroup(groupId: String, userList: String): String = {
    "{\"groupId\":\"" + groupId + "\",\"userList\":" + userList + "}"
  }
}
