package de.upb.cs.uc4.hyperledger.tests.general

import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionCertificate
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataMatriculation, TestHelper, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager }
import org.hyperledger.fabric_ca.sdk.HFCAClient
import org.scalatest.matchers.should.Matchers.be

import scala.io.Source

class UserManagementTests extends TestBase {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
  }

  "The enrollmentManager" when {
    "enrolling a User without csr" should {
      "allow for the new User to access the chain [UserManagement_300]" in {
        super.tryAdminEnrollment(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)
        val connection = super.initializeCertificate(username)

        val testData = "UserManagement_300"
        TestHelper.testAddCertificateAccess(testData, connection)
      }
      "allow for the new User to matriculate themselves [UserManagement_301]" in {
        val testUser = "UserManagement_301"

        // register test user 301
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUser, username, walletPath, "org1")
        // enroll test user 301
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUser, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)

        val testData: String = TestDataMatriculation.validMatriculationData1(testUser)
        // add approval as admin user
        initializeOperation(username).initiateOperation(username, "UC4.MatriculationData", "addMatriculationData", testData)

        // access chaincode as test user 301
        val matriculationConnectionUser = super.initializeMatriculation(testUser)
        matriculationConnectionUser.addMatriculationData(testData)
        matriculationConnectionUser.close()

      }
    }
    "enrolling a User with csr" should {
      "successfully enroll the user and provide a certificate in the wallet." in {
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
  }

  "The registrationManager" when {
    "performing a registration [UserManagement_401]" should {
      "not throw exceptions" in {
        Logger.info("Enroll as admin and store cert to wallet")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Register TestUser")
        val testUserName = "UserManagement_401"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.info("Enroll TestUser")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)

        Logger.info("Access Chain as TestUser")
        val testCert = "whatever"
        val connection = super.initializeCertificate(testUserName)
        initializeOperation().initiateOperation(testUserName, connection.contractName, "updateCertificate", testUserName, testCert)
        connection.updateCertificate(testUserName, testCert)
        initializeOperation().initiateOperation(testUserName, connection.contractName, "getCertificate", testUserName)
        val result: String = connection.getCertificate(testUserName)
        result should be(testCert)
      }
    }
  }
}
