package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class UnsignedTransactionTests extends TestBase {

  var chaincodeConnection: ConnectionCertificateTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeCertificate()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned transaction" should {
      "return an unsigned transaction" in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        val proposal = chaincodeConnection.createUnsignedTransaction("addCertificate", enrollmentId, certificate)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader().toStringUtf8())
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload().toStringUtf8())
      }
    }
  }
}
