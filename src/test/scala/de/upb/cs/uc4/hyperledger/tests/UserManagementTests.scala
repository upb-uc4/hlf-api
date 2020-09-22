package de.upb.cs.uc4.hyperledger.tests


import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric_ca.sdk.HFCAClient

import scala.io.Source

class UserManagementTests extends TestBase {

  "The enrollmentManager" when {
    "enrolling a User without csr" should {
      "allow for the new User to access the chain [101]" in {
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId)

        val connection = super.initializeCourses(username)
        TestHelper.testChaincodeCourseAccess("101", connection)
      }
    }
    "enrolling a User with csr" should {
      "not directly allow for the new User to access the chain [102]" in {
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

        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId, content)
        Logger.info("Finished enrolling new user")

        WalletManager.containsIdentity(walletPath, testUserName) should be(true)

        Logger.debug("Newly enrolled Identity: " + WalletManager.getIdentity(walletPath, testUserName).toString)

        intercept[Exception](super.initializeCourses(testUserName))
      }
    }
  }

  "The registrationManager" when {
    "performing a registration [103]" should {
      "not throw exceptions" in {
        Logger.info("Enroll as admin and store cert to wallet")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId)

        Logger.info("Register TestUser")
        val testUserName = "Tester102"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.info("Enroll TestUser")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId)

        Logger.info("Access Chain as TestUser")
        val connection = super.initializeCourses(testUserName)
        TestHelper.testChaincodeCourseAccess("103", connection)
      }
    }
  }
}
