package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataMatriculation, TestHelper }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric_ca.sdk.HFCAClient

import scala.io.Source

class UserManagementTests extends TestBase {

  private def testEnrollmentException(signedCert: String, f: () => Any) = {
    val result = intercept[HyperledgerExceptionTrait](f.apply())
    //result.content should not be(signedCert)
    assert(result.getMessage === "Error")
  }

  "The enrollmentManager" when {
    "enrolling a User without csr" should {
      "allow for the new User to access the chain [300]" in {
        val enrollmentID = "300"
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)
        val connection = super.initializeCertificate(username)

        TestHelper.testAddCertificateAccess(enrollmentID, connection)
      }
      "allow for the new User to matriculate himself [301]" in {
        val enrollmentID = "301"
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        // register test user 301
        val testUserPw = RegistrationManager.register(caURL, tlsCert, enrollmentID, username, walletPath, "org1")
        // enroll test user 301
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, enrollmentID, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)

        // access chaincode as test user 301
        val matriculationConnectionUser = super.initializeMatriculation(enrollmentID)
        matriculationConnectionUser.addMatriculationData(TestDataMatriculation.validMatriculationData1(enrollmentID))
        matriculationConnectionUser.close();

        // access chaincode as admin user
        // TODO: enable once dualSigning is supported
        // val matriculationConnectionAdmin = super.initializeMatriculation(username)
        // matriculationConnectionAdmin.addMatriculationData(TestDataMatriculation.validMatriculationData1(enrollmentID))
        // matriculationConnectionAdmin.close();
      }
    }
    "enrolling a User with a valid csr" should {
      "successfully return the signed certificate [testid]" in {
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser")
        val testUserName = "testid"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.debug("get csr_pem")
        val resource = getClass.getResource("/testid.csr")
        Logger.debug(s"file: ${resource.getFile}")
        val source = Source.fromURL(resource)
        var content: String = null
        try {
          content = source.mkString
        }
        finally {
          source.close()
        }
        Logger.debug(s"content: $content")

        val signedCert: String = EnrollmentManager.enrollSecure(caURL, tlsCert, testUserName, testUserPw, content, adminName = username, adminWalletPath = walletPath, channel, chaincode, networkDescriptionPath)
        Logger.info("Finished enrolling new user")

        signedCert should not be null
      }
    }
    "enrolling a User with an invalid csr" should {
      "throw an exception [testid, testId2]" in {
        super.tryEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser and EvilUser")
        val testUserName = "testid"
        val evilUserName = "testid2"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)
        val evilUserPw = RegistrationManager.register(caURL, tlsCert, evilUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.debug("get csr_pem")
        val resource = getClass.getResource("/testid.csr")
        Logger.debug(s"file: ${resource.getFile}")
        val source = Source.fromURL(resource)
        var content: String = null
        try {
          content = source.mkString
        }
        finally {
          source.close()
        }
        Logger.debug(s"content: $content")

        testEnrollmentException(
          "enrollSecure",
          () => EnrollmentManager.enrollSecure(caURL, tlsCert, evilUserName, evilUserPw, content, adminName = username, adminWalletPath = walletPath, channel, chaincode, networkDescriptionPath)
        )

        //val signedCert: String = EnrollmentManager.enrollSecure(caURL, tlsCert, evilUserName, evilUserPw, content, adminName = username, adminWalletPath = walletPath, channel, chaincode, networkDescriptionPath)
        // Logger.info("Finished enrolling evil user with test user's csr")

        //signedCert should not be null// throw exception
      }
    }
  }

  "The registrationManager" when {
    "performing a registration [302]" should {
      "not throw exceptions" in {
        val enrollmentID = "302"
        Logger.info("Enroll as admin and store cert to wallet")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser")
        val testUserName = s"Tester$enrollmentID"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.info("Enroll TestUser")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Access Chain as TestUser")
        val connection = super.initializeCertificate(testUserName)
        TestHelper.testAddCertificateAccess(enrollmentID, connection)
      }
    }
  }
}
