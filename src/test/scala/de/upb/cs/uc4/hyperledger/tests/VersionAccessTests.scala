package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase

import scala.util.matching.Regex

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

  "The ScalaAPI for Connections" when {
    "asked for chaincode version " should {
      "provide a valid endpoint [Certificate] " in {
        val version: String = certificateConnection.getVersion
        val regex = new Regex("v((\\d)+\\.){2}(\\d)+")
        regex.matches(version) should be
      }
      "provide a valid endpoint [Certificate] " in {
        val version: String = matriculationConnection.getVersion
        val regex = new Regex("v((\\d)+\\.){2}(\\d)+")
        regex.matches(version) should be
      }
    }
  }
}