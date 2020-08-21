import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, WalletManager }

class EnrollmentTests extends TestBase {

  "The enrollmentManager" when {
    "beginning enrollment" should {

      "work as expected" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(walletPath)
        wallet should not be null

        println(s"CA-url: $caURL")
        println(tlsCert.toAbsolutePath)

        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId)

        val connection = initializeCourses()
        connection.addCourse("{\"courseId\":\"" + "1" + "\",\"courseName\":\"IQC\",\"courseType\":\"Lecture\",\"startDate\":\"1998-01-01\",\"endDate\":\"1999-01-01\",\"ects\":7,\"lecturerId\":\"Mustermann\",\"maxParticipants\":80,\"currentParticipants\":20,\"courseLanguage\":\"English\",\"courseDescription\":\"Fun new course\"}")
        val result = connection.getCourseById("1")
        println(result)
      }
    }
  }
}
