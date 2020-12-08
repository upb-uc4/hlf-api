package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionAdmission, ConnectionApproval, ConnectionCertificate, ConnectionExaminationRegulation, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionApprovalsTrait, ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, WalletManager }

class TestBase extends TestBaseTrait {
  private val testBase: TestBaseTrait = sys.env.getOrElse("UC4_TESTBASE_TARGET", "not relevant") match {
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

  protected[hyperledger] def tryEnrollment(
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
    debug("Try Enrollment")
    if (!WalletManager.containsIdentity(this.walletPath, this.username)) {
      try {
        debug("Try enrollment with: "
          + " " + caURL
          + " " + caCert
          + " " + walletPath
          + " " + enrollmentID
          + " " + enrollmentSecret
          + " " + organisationId)

        EnrollmentManager.enroll(caURL, caCert, walletPath, enrollmentID, enrollmentSecret, organisationId, channel, chaincode, networkDescriptionPath)
      }
      catch {
        case e: Exception => Logger.warn("Enrollment failed, maybe some other test already enrolled the admin: " + e.getMessage)
      }
    }
    debug("Finished Enrollment")
  }

  override def beforeAll(): Unit = {
    debug("Begin test with testBase Name = " + testBase.getClass.getName)
    if (testBase.isInstanceOf[TestBaseProductionNetwork]) {
      tryEnrollment(this.caURL, this.tlsCert, this.walletPath, this.username, this.password, this.organisationId, this.channel, this.chaincode, this.networkDescriptionPath)
    }
  }

  def initializeMatriculation(userName: String = this.username): ConnectionMatriculationTrait = ConnectionMatriculation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeCertificate(userName: String = this.username): ConnectionCertificateTrait = ConnectionCertificate(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeApproval(userName: String = this.username): ConnectionApprovalsTrait = ConnectionApproval(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeExaminationRegulation(userName: String = this.username): ConnectionExaminationRegulationTrait = ConnectionExaminationRegulation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeAdmission(userName: String = this.username): ConnectionAdmissionTrait = ConnectionAdmission(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)

  private def debug(message: String): Unit = {
    Logger.debug("[TestBase] :: " + message)
  }
}