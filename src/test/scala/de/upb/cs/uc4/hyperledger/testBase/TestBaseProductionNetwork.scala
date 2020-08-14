package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.connections.cases.{ConnectionCourses, ConnectionMatriculation}
import de.upb.cs.uc4.hyperledger.connections.traits.{ConnectionCourseTrait, ConnectionMatriculationTrait}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBaseProductionNetwork extends AnyWordSpec with Matchers with BeforeAndAfterEach{

  val network_description_path: Path = Paths.get(getClass.getResource("/connection_profile_kubernetes.yaml").toURI)
  val wallet_path: Path = Paths.get(getClass.getResource("/wallet/").toURI)
  val minikubeIP: String = sys.env.getOrElse("MINIKUBE_IP", "172.17.0.2")
  val ca_url = s"https://${minikubeIP}:30907"
  val tlsCert: Path = Paths.get("/tmp/hyperledger/org1/msp/cacerts/org1-ca-cert.pem")
  val username: String = "scala-admin-org1"
  val password: String = "scalaAdminPW"
  val organisationId: String = "org1MSP"
  val channel: String = "mychannel"
  val chaincode: String = "uc4-cc"

  def initializeCourses(): ConnectionCourseTrait = ConnectionCourses(username, channel, chaincode, wallet_path, network_description_path)
  def initializeMatriculation(): ConnectionMatriculationTrait = ConnectionMatriculation(username, channel, chaincode, wallet_path, network_description_path)
}
