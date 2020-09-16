package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionCourses, ConnectionMatriculation }
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCourseTrait, ConnectionMatriculationTrait }

protected class TestBaseProductionNetwork extends TestBaseTrait {

  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile_kubernetes.yaml").toURI)
  val minikubeIP: String = sys.env.getOrElse("MINIKUBE_IP", "172.17.0.2")
  val caURL = s"https://$minikubeIP:30907"
  val tlsCert: Path = Paths.get("/tmp/hyperledger/org1/msp/cacerts/org1-ca-cert.pem")
  val username: String = "scala-registration-admin-org1"
  val password: String = "scalaAdminPW"
  val organisationId: String = "org1MSP"
  val channel: String = "mychannel"
  val chaincode: String = "uc4-cc"

  def initializeCourses(): ConnectionCourseTrait = new ConnectionCourses(username, channel, chaincode, walletPath, networkDescriptionPath)
  def initializeMatriculation(): ConnectionMatriculationTrait = ConnectionMatriculation(username, channel, chaincode, walletPath, networkDescriptionPath)
}
