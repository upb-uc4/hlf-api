import java.nio.file.{Path, Paths}

import de.upb.cs.uc4.hyperledger.utilities.{EnrollmentManager, WalletManager}
import de.upb.cs.uc4.hyperledger.ConnectionManager
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EnrollmentTests extends AnyWordSpec with Matchers {

  val connection_profile_path: Path = Paths.get(getClass.getResource("/connection_profile.yaml").toURI)
  val wallet_path: Path = Paths.get(getClass.getResource("/wallet/").toURI)
  val minikubeIP = "172.17.0.2" // TODO:
  val ca_url = s"https://${minikubeIP}:30907" // TODO:
  val tlsCert: Path = Paths.get("../HLF-Production-Network/tmp/hyperledger/org1/ca/crypto/ca-cert.pem")
  val username: String = "scala-admin-org1"
  val password: String = "scalaAdminPW"
  val organisationId: String = "org1MSP"
  val channel: String = "channel"
  val chaincode: String = "chaincode"
  // val organisationId : String = "org1"

  "The enrollmentManager" when {
    "beginning enrollment" should {

      "work as expected" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(wallet_path)
        wallet should not be null

        println(s"CA-url: $ca_url")
        println(tlsCert.toAbsolutePath)

        EnrollmentManager.enroll(ca_url, tlsCert, wallet_path, username, password, organisationId)

        val connection = ConnectionManager(connection_profile_path, wallet_path).createConnection(username, channel, chaincode)

        val result = connection.getAllCourses()
        result should ===("[]")
      }
    }
  }
}
