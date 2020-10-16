package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class CertificateAccessTests extends TestBase {

  var chaincodeConnection: ConnectionCertificateTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeCertificate()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for Certificates" when {
    "invoked with correct transactions " should {
      "allow for adding new Certificate " in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        TestHelper.compareCertificates(certificate, chaincodeConnection.addCertificate(enrollmentId, certificate))
      }
      "allow for reading Certificate " in {
        chaincodeConnection.getCertificate("100")
      }
      "read the correct data " in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        TestHelper.compareJson(certificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "allow for updating existing Data " in {
        val enrollmentId = "100"
        val newCertificate = "Whatever2"
        TestHelper.compareJson(newCertificate, chaincodeConnection.updateCertificate(enrollmentId, newCertificate))
        TestHelper.compareJson(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "support update on addOrUpdate-Command " in {
        val enrollmentId = "100"
        val newCertificate = "Whatever3"
        TestHelper.compareJson(newCertificate, chaincodeConnection.addOrUpdateCertificate(enrollmentId, newCertificate))
        TestHelper.compareJson(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "support add on addOrUpdate-Command " in {
        val enrollmentId = "101"
        val newCertificate = "Whatever1"
        TestHelper.compareJson(newCertificate, chaincodeConnection.addOrUpdateCertificate(enrollmentId, newCertificate))
        TestHelper.compareJson(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
    }
  }
}