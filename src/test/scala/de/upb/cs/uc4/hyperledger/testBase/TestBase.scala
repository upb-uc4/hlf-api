package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionAdmission, ConnectionApproval, ConnectionCertificate, ConnectionExaminationRegulation, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionApprovalsTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric.gateway.Wallet
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl

class TestBase extends TestBaseTrait {
  private val testBase: TestBaseTrait = sys.env.getOrElse("UC4_TESTBASE_TARGET", "not relevant").trim() match {
    case "PRODUCTION_NETWORK" => new TestBaseProductionNetwork
    case _                    => new TestBaseDevNetwork
  }
  override val networkDescriptionPath: Path = testBase.networkDescriptionPath
  override val caURL: String = testBase.caURL
  override val tlsCert: Path = testBase.tlsCert
  override val username: String = testBase.username
  override val password: String = testBase.password
  override val organisationId: String = testBase.organisationId
  override val channel: String = testBase.channel
  override val chaincode: String = testBase.chaincode

  override def beforeAll(): Unit = {
    super.beforeAll()
    Logger.debug("Begin test with testBase Name = " + testBase.getClass.getName)
    if (testBase.isInstanceOf[TestBaseProductionNetwork]) {
      tryAdminEnrollment(this.caURL, this.tlsCert, this.walletPath, this.username, this.password, this.organisationId, this.channel, this.chaincode, this.networkDescriptionPath)
    }
  }

  def initializeMatriculation(userName: String = this.username): ConnectionMatriculationTrait = ConnectionMatriculation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeCertificate(userName: String = this.username): ConnectionCertificateTrait = ConnectionCertificate(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeApproval(userName: String = this.username): ConnectionApprovalsTrait = ConnectionApproval(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeExaminationRegulation(userName: String = this.username): ConnectionExaminationRegulationTrait = ConnectionExaminationRegulation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeAdmission(userName: String = this.username): ConnectionAdmissionTrait = ConnectionAdmission(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)

  def tryRegisterAndEnrollTestUser(enrollmentId: String, affiliation: String): X509IdentityImpl = {
    try {
      val testUserPw = RegistrationManager.register(caURL, tlsCert, enrollmentId, username, walletPath, affiliation)
      EnrollmentManager.enroll(caURL, tlsCert, walletPath, enrollmentId, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)
    }
    catch {
      case _: Throwable =>
    }
    val wallet: Wallet = WalletManager.getWallet(this.walletPath)
    wallet.get(enrollmentId).asInstanceOf[X509IdentityImpl]
  }

  protected[hyperledger] def tryAdminEnrollment(
      caURL: String,
      caCert: Path,
      walletPath: Path,
      enrollmentID: String,
      enrollmentSecret: String,
      organisationId: String,
      channel: String,
      chaincode: String,
      networkDescriptionPath: Path
  ): Unit = {
    if (!WalletManager.containsIdentity(this.walletPath, this.username)) {
      try {
        EnrollmentManager.enroll(caURL, caCert, walletPath, enrollmentID, enrollmentSecret, organisationId, channel, chaincode, networkDescriptionPath)
      }
      catch {
        case e: Exception => Logger.warn("Enrollment failed, maybe some other test already enrolled the admin: " + e.getMessage)
      }
    }
  }
}
