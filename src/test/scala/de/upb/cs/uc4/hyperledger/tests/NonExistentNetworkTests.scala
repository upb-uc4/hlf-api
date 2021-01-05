package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCertificate, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.exceptions.traits.NetworkExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation
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
      organisationId: String = null
  ) = {
    val result: NetworkExceptionTrait = intercept[NetworkExceptionTrait](f.apply())

    // check resulting exception
    result.channel should (be(channel) or be(null))
    result.chaincode should (be(chaincode) or be(null))
    result.networkDescription should (be(networkDescription) or be(null))
    result.identity should (be(identity) or be(null))
    result.organisationId should (be(organisationId) or be(null))
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

  "The public Managers" when {
    "no network is running / reachable" should {
      "throw NetworkErrors [EnrollmentManager Basic]" in {
        val newUserName = "NoNetwork001"
        this.testNetworkException(() => {
          EnrollmentManager.enroll(caURL, tlsCert, walletPath, newUserName, password,
            organisationId, channel, chaincode, networkDescriptionPath)
        }, channel, chaincode, networkDescriptionPath.toString, newUserName, organisationId)
      }
      "throw NetworkErrors [EnrollmentManager Secure]" in {
        val testUserName = "testid"
        val testUserPw = "testPassword"
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

        this.testNetworkException(() => {
          RegistrationManager.register(caURL, tlsCert, testUserName, username, walletPath,
            1, HFCAClient.HFCA_TYPE_CLIENT)
        }, identity = username)
      }
      "throw NetworkErrors [WalletManager]" in {
        val newUserName = "NoNetwork002"
        this.testNetworkException(() => {
          WalletManager.getCertificate(walletPath, newUserName)
        }, identity = newUserName)
      }
      "throw NetworkErrors [ConnectionManager - Matriculation]" in {
        this.testNetworkException(() => {
          val connection = ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
          connection.addMatriculationData(TestDataMatriculation.validMatriculationData1("0101010"))
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [ConnectionManager - Certificate]" in {
        this.testNetworkException(() => {
          val newUserName = "NoNetwork003"
          val connection = ConnectionCertificate(username, channel, chaincode, walletPath, networkDescriptionPath)
          connection.addCertificate(newUserName, newUserName)
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [getVersion - Matriculation]" in {
        this.testNetworkException(() => {
          val connection = ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
          connection.getChaincodeVersion
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
      "throw NetworkErrors [getVersion - Certificate]" in {
        this.testNetworkException(() => {
          val connection = ConnectionCertificate(username, channel, chaincode, walletPath, networkDescriptionPath)
          connection.getChaincodeVersion
        }, channel, chaincode, networkDescriptionPath.toString, username)
      }
    }
  }
}
