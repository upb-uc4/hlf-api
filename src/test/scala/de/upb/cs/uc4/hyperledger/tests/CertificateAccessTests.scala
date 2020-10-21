package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.TestHelper

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
      "allow for adding new Certificate [200] " in {
        val enrollmentId = "200"
        val certificate = "Whatever"
        TestHelper.compareCertificates(certificate, chaincodeConnection.addCertificate(enrollmentId, certificate))
      }
      "allow for reading Certificate [200] " in {
        val enrollmentId = "200"
        chaincodeConnection.getCertificate(enrollmentId)
      }
      "read the correct data [200] " in {
        val enrollmentId = "200"
        val certificate = "Whatever"
        TestHelper.compareCertificates(certificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "allow for updating existing Data [200] " in {
        val enrollmentId = "200"
        val newCertificate = "Whatever2"
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.updateCertificate(enrollmentId, newCertificate))
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "support update on addOrUpdate-Command [200] " in {
        val enrollmentId = "200"
        val newCertificate = "Whatever3"
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.addOrUpdateCertificate(enrollmentId, newCertificate))
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
      "support add on addOrUpdate-Command [201] " in {
        val enrollmentId = "201"
        val newCertificate = "Whatever1"
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.addOrUpdateCertificate(enrollmentId, newCertificate))
        TestHelper.compareCertificates(newCertificate, chaincodeConnection.getCertificate(enrollmentId))
      }
    }
  }
}