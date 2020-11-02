package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionApproval, ConnectionCertificate, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionApprovalsTrait, ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, WalletManager }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

class TestBase extends TestBaseTrait {
  private val testBase: TestBaseTrait = tryRetrieveEnvVar("Target") match {
    case "ProductionNetwork" => new TestBaseProductionNetwork
    case _                   => new TestBaseDevNetwork
  }
  override val networkDescriptionPath: Path = testBase.networkDescriptionPath
  override val caURL: String = testBase.caURL
  override val tlsCert: Path = testBase.tlsCert
  override val username: String = testBase.username
  override val password: String = testBase.password
  override val organisationId: String = testBase.organisationId
  override val channel: String = testBase.channel
  override val chaincode: String = testBase.chaincode

  private def tryEnrollment(
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

  override def beforeAll(): Unit = {
    debug("Begin test with testBase Name = " + testBase.getClass.getName)
    if (testBase.isInstanceOf[TestBaseProductionNetwork]) {
      while (!WalletManager.containsIdentity(this.walletPath, this.username)) {
        tryEnrollment(
          this.caURL,
          this.tlsCert,
          this.walletPath,
          this.username,
          this.password,
          this.organisationId,
          this.channel,
          this.chaincode,
          this.networkDescriptionPath
        )
      }
      debug("Finished Enrollment")
    }
  }

  def initializeMatriculation(userName: String = this.username): ConnectionMatriculationTrait = ConnectionMatriculation(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeCertificate(userName: String = this.username): ConnectionCertificateTrait = ConnectionCertificate(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
  def initializeApproval(userName: String = this.username): ConnectionApprovalsTrait = ConnectionApproval(userName, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)

  private def tryRetrieveEnvVar(varName: String, fallBack: String = ""): String = {
    if (sys.env.contains(varName)) {
      val value = sys.env(varName)
      debug("####### Retrieved variable: " + varName + " with value: " + value)
      value
    }
    else {
      debug("####### Returned default fallback")
      fallBack
    }
  }

  private def debug(message: String): Unit = {
    Logger.debug("[TestBase] :: " + message)
  }
}