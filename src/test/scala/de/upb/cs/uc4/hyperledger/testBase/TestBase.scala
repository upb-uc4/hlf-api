package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.Path
import java.security.PrivateKey

import de.upb.cs.uc4.hyperledger.testUtil.TestSetup
import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionAdmission, ConnectionCertificate, ConnectionExam, ConnectionExamResult, ConnectionExaminationRegulation, ConnectionGroup, ConnectionMatriculation, ConnectionOperation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionExamResultTrait, ConnectionExamTrait, ConnectionExaminationRegulationTrait, ConnectionGroupTrait, ConnectionMatriculationTrait, ConnectionOperationTrait }
import de.upb.cs.uc4.hyperledger.utilities.helper.{ CryptoHelper, Logger }
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }
import org.hyperledger.fabric.gateway.X509Identity

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
    val debugMsg: String = "Begin test with testBase Name = " + testBase.getClass.getName + " test:: " + this.getClass.getName
    Logger.debug(debugMsg)
    if (testBase.isInstanceOf[TestBaseProductionNetwork]) {
      tryAdminEnrollment(this.caURL, this.tlsCert, this.walletPath, this.username, this.password, this.organisationId, this.channel, this.chaincode, this.networkDescriptionPath)
      Thread.sleep(5000) // wait for enrollment to finish
      TestSetup.establishAdminAndSystemGroup(initializeGroup(), username)
    }
  }

  /// initialize methods
  def initializeAdmission(userName: String = this.username): ConnectionAdmissionTrait = ConnectionAdmission(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeCertificate(userName: String = this.username): ConnectionCertificateTrait = ConnectionCertificate(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeExaminationRegulation(userName: String = this.username): ConnectionExaminationRegulationTrait = ConnectionExaminationRegulation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeExam(userName: String = this.username): ConnectionExamTrait = ConnectionExam(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeExamResult(userName: String = this.username): ConnectionExamResultTrait = ConnectionExamResult(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeGroup(userName: String = this.username): ConnectionGroupTrait = ConnectionGroup(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeMatriculation(userName: String = this.username): ConnectionMatriculationTrait = ConnectionMatriculation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeOperation(userName: String = this.username): ConnectionOperationTrait = ConnectionOperation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)

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
        case e: Exception => Logger.warn("Admin Enrollment failed, maybe some other test already enrolled the admin: " + e.getMessage)
      }
    }
  }

  protected def prepareUsers(userName: Seq[String]): Unit = userName.foreach(name => prepareUser(name))
  protected def prepareUser(userName: String): (PrivateKey, String) = {
    Logger.info(s"prepare User:: $userName")
    // get testUser certificate and private key
    val testUserIdentity: X509Identity = ensureEnrolled(userName, organisationId)
    val privateKey: PrivateKey = testUserIdentity.getPrivateKey
    val certificatePem: String = CryptoHelper.toPemString(testUserIdentity.getCertificate)

    (privateKey, certificatePem)
  }

  /*
    Ensures the user in question is enrolled - enrolls them manually if not
   */
  def ensureEnrolled(enrollmentId: String, affiliation: String): X509Identity = {
    if (WalletManager.containsIdentity(this.walletPath, enrollmentId)) {
      WalletManager.getX509Identity(this.walletPath, enrollmentId)
    }
    else {
      val testUserPw = RegistrationManager.register(caURL, tlsCert, enrollmentId, username, walletPath, affiliation)
      EnrollmentManager.enroll(caURL, tlsCert, walletPath, enrollmentId, testUserPw, organisationId, channel, chaincode, networkDescriptionPath)
      WalletManager.getX509Identity(this.walletPath, enrollmentId)
    }
  }
}
