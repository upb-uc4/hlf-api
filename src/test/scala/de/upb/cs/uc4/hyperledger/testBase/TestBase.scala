package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.cases.{ConnectionCertificate, ConnectionCourses, ConnectionMatriculation}
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCertificateTrait, ConnectionCourseTrait, ConnectionMatriculationTrait}
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.utilities.EnrollmentManager
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.scalactic.Fail

import scala.reflect.internal.util.NoFile.input

class TestBase extends TestBaseTrait {
  private val testBase: TestBaseTrait = tryRetrieveEnvVar("Target") match {
    case "ProductionNetwork" => new TestBaseProductionNetwork
    case _                   => new TestBaseDevNetwork
  }
  override val networkDescriptionPath: Path = testBase.networkDescriptionPath
  override val minikubeIP: String = testBase.minikubeIP
  override val caURL: String = testBase.caURL
  override val tlsCert: Path = testBase.tlsCert
  override val username: String = testBase.username
  override val password: String = testBase.password
  override val organisationId: String = testBase.organisationId
  override val channel: String = testBase.channel
  override val chaincode: String = testBase.chaincode

  override def beforeAll(): Unit = {
    debug("Begin test with testBase Name = " + testBase.getClass.getName)
    if (testBase.isInstanceOf[TestBaseProductionNetwork]) {
      debug("Begin enrollment with: "
        + " " + caURL
        + " " + tlsCert
        + " " + walletPath
        + " " + username
        + " " + password
        + " " + organisationId)
      EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId, channel, chaincode, networkDescriptionPath)
      debug("Finished Enrollment")
    }
  }

  def initializeCourses(userName: String = testBase.username): ConnectionCourseTrait = new ConnectionCourses(userName, testBase.channel, testBase.chaincode, testBase.walletPath, testBase.networkDescriptionPath)
  def initializeMatriculation(userName: String = testBase.username): ConnectionMatriculationTrait = ConnectionMatriculation(userName, testBase.channel, testBase.chaincode, testBase.walletPath, testBase.networkDescriptionPath)
  def initializeCertificate(userName: String = testBase.username): ConnectionCertificateTrait = ConnectionCertificate(userName, testBase.channel, testBase.chaincode, testBase.walletPath, testBase.networkDescriptionPath)

  private def tryRetrieveEnvVar(varName: String, fallBack: String = ""): String = {
    if (sys.env.contains(varName)) {
      val value = sys.env(varName)
      debug("####### Retrieved variable: " + varName + " with value: " + value)
      value
    } else {
      debug("####### Returned default fallback")
      fallBack
    }
  }

  private def debug(message: String): Unit = {
    Logger.debug("[TestBase] :: " + message)
  }
}