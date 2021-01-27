package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionExaminationRegulationTrait, ConnectionMatriculationTrait, ConnectionOperationTrait, ConnectionTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class VersionAccessTests extends TestBase {

  val regexVersionString: String = """((\d)+\.){2}(\d)+"""

  def testVersion(connection: ConnectionTrait): Unit = {
    val version: String = connection.getChaincodeVersion
    version should fullyMatch regex regexVersionString
    connection.close()
  }

  "The ScalaAPI for Connections" when {
    "asked for chaincode version " should {
      "provide a valid endpoint [Admission] " in {
        testVersion(initializeAdmission())
      }
      "provide a valid endpoint [Certificate] " in {
        testVersion(initializeCertificate())
      }
      "provide a valid endpoint [ExaminationRegulation] " in {
        testVersion(initializeExaminationRegulation())
      }
      "provide a valid endpoint [Group] " in {
        testVersion(initializeGroup())
      }
      "provide a valid endpoint [Matriculation] " in {
        testVersion(initializeMatriculation())
      }
      "provide a valid endpoint [Operation] " in {
        testVersion(initializeOperation())
      }
    }
  }
}