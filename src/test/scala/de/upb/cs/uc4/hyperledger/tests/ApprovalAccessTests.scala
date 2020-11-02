package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataMatriculation, TestHelper }

class ApprovalAccessTests extends TestBase {

  var chaincodeConnection: ConnectionApprovalsTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeApproval()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Approvals" when {
    "invoked with correct transactions " should {
      "allow for adding new AprovalData " in {
        val newData = "Test Parameter"
        chaincodeConnection.approveTransaction("UC4.Certificate", "addCertificate", "000001", "totally valid cert")
      }
    }
  }
}