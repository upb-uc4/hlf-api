package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.connections.cases.{ConnectionCourses, ConnectionMatriculation}
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCourseTrait, ConnectionMatriculationTrait}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBaseDevNetwork extends AnyWordSpec with Matchers with BeforeAndAfterEach{
  val network_description_path: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val wallet_path: Path = Paths.get(getClass.getResource("/wallet/").toURI)
  val id: String = "cli"
  val channel: String = "myc"
  val chaincode: String = "mycc"

  def initializeCourses(): ConnectionCourseTrait = new ConnectionCourses(id, channel, chaincode, wallet_path, network_description_path)
  def initializeMatriculation(): ConnectionMatriculationTrait = new ConnectionMatriculation(id, channel, chaincode, wallet_path, network_description_path)


}
