package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase

class VersionAccessTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  var regexVersionString: String = """v((\d)+\.){2}(\d)+"""

  "The ScalaAPI for Connections" when {
    "asked for chaincode version " should {
      "provide a valid endpoint [Certificate] " in {
        val version: String = certificateConnection.getVersion
        version should fullyMatch regex regexVersionString
      }
      "provide a valid endpoint [Certificate] " in {
        val version: String = matriculationConnection.getVersion
        version should fullyMatch regex regexVersionString
      }
    }
  }
}