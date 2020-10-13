package de.upb.cs.uc4.hyperledger.testBase

import java.nio.file.{ Path, Paths }

protected class TestBaseProductionNetwork extends TestBaseTrait {
  val networkDescriptionPath: Path = Paths.get(getClass.getResource("/connection_profile_kubernetes.yaml").toURI)
  val username: String = "scala-registration-admin-org1"
  val channel: String = "mychannel"
  val chaincode: String = "uc4-cc"
  // productionNetwork variables
  val minikubeIP: String = sys.env.getOrElse("NODE_IP", "172.17.0.2")
  val caURL = s"https://$minikubeIP:30907"
  val tlsCert: Path = Paths.get("/tmp/hyperledger/org1/msp/cacerts/org1-ca-cert.pem")
  val password: String = "scalaAdminPW"
  val organisationId: String = "org1MSP"
}
