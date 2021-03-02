package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionGroupTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.TestHelperStrings
import de.upb.cs.uc4.hyperledger.testData.TestDataGroup
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class GroupTests extends TestBase {

  var chaincodeConnection: ConnectionGroupTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeGroup()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Groups" when {
    "invoking the addUserToGroup transaction " should {
      val testData: Seq[(String, String, String)] = Seq(
        ("allow for adding a new User to a new Group", "100", "someGroup"),
        ("allow for adding a new User to an existing Group", "101", "someGroup"),
        ("allow for adding an existing User to a Group twice", "100", "someGroup"),
      )
      for ((statement: String, enrollmentId: String, groupId: String) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          prepareUser(enrollmentId)
          chaincodeConnection.addUserToGroup(enrollmentId, groupId)
        }
      }
    }
    "invoking the getAllGroups transaction " should {
      "return existing Groups" in {
        chaincodeConnection.getAllGroups
      }
    }
    "invoking the getUsersForGroup transaction" should {
      "return the Group's Users" in {
        TestHelperStrings.compareJson(TestDataGroup.userList1, chaincodeConnection.getUsersForGroup("someGroup"))
      }
    }
    "invoking the getGroupsForUser transaction" should {
      "return the User's Groups" in {
        TestHelperStrings.compareJson(TestHelperStrings.getJsonList(Seq[String]("\"someGroup\"")), chaincodeConnection.getGroupsForUser("100"))
      }
    }
    "invoking the removeUserFromGroup transaction " should {
      val testData: Seq[(String, String, String)] = Seq(
        ("allow for removing an existing User from a Group", "101", "someGroup")
      )
      for ((statement: String, enrollmentId: String, groupId: String) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          chaincodeConnection.removeUserFromGroup(enrollmentId, groupId)
        }
      }
    }
    "invoking the removeUserFromAllGroups transaction " should {
      val testData: Seq[(String, String)] = Seq(
        ("allow for removing an existing User from all Groups", "100"),
      )
      for ((statement: String, enrollmentId: String) <- testData) {
        s"$statement" in {
          Logger.info("Begin test: " + statement)
          chaincodeConnection.removeUserFromAllGroups(enrollmentId)
        }
      }
    }
  }
}