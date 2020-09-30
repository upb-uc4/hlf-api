package de.upb.cs.uc4.hyperledger.tests

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCertificate, ConnectionCourses, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.exceptions.traits.NetworkExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric_ca.sdk.HFCAClient

import scala.io.Source

class NonExistentNetworkTests extends TestBase {

  override def beforeAll(): Unit = {
    //  do nothing to undo enrollment
  }

  private def testNetworkException(
      f: () => Any,
      channel: String = null,
      chaincode: String = null,
      networkDescription: String = null,
      identity: String = null,
      organisationId: String = null,
      organisationName: String = null
  ) = {
    val result: NetworkExceptionTrait = intercept[NetworkExceptionTrait](f.apply())

    // check resulting exception
    result.channel should (be(channel) or be(null))
    result.chaincode should (be(chaincode) or be(null))
    result.networkDescription should (be(networkDescription) or be(null))
    result.identity should (be(identity) or be(null))
    result.organisationId should (be(organisationId) or be(null))
    result.organisationName should (be(organisationName) or be(null))
  }

  private def getCSR: String = {
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
    content
  }

  "The Managers" when {
    "no network is running / reachable" should {
      "throw NetworkErrors [EnrollmentManager]" in {
        this.testNetworkException(() => {
          EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password,
            organisationId, channel, chaincode, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, username, organisationId)
      }
      "throw NetworkErrors [EnrollmentManager]" in {
        val testUserName = "testid"
        val testUserPw = RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, "org1", 1, HFCAClient.HFCA_TYPE_CLIENT)

        Logger.debug("get csr_pem")
        val content = getCSR
        Logger.debug(s"content: $content")

        this.testNetworkException(() => {
          EnrollmentManager.enrollSecure(caURL, tlsCert, testUserName, testUserPw,
            content, adminName = username, adminWalletPath = walletPath, channel, chaincode, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [RegistrationManager]" in {
        Logger.info("Register Tester102")
        val testUserName = "Tester102"
        val affiliation = "org1"

        this.testNetworkException(() => {
          RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath, affiliation,
            1, HFCAClient.HFCA_TYPE_CLIENT)
        }, identity = username, organisationId = affiliation)
      }
      "throw NetworkErrors [WalletManager]" in {
        this.testNetworkException(() => {
          WalletManager.getCertificate(walletPath, username)
        }, identity = username)
      }
      "throw NetworkErrors [ConnectionManager]" in {
        this.testNetworkException(() => {
          ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [ConnectionManager]" in {
        this.testNetworkException(() => {
          new ConnectionCourses(username, channel, chaincode, walletPath, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [ConnectionManager]" in {
        this.testNetworkException(() => {
          ConnectionCertificate(username, channel, chaincode, walletPath, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
    }
  }
}
