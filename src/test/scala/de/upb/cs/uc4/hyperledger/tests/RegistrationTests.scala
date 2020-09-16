package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.{ EnrollmentManager, RegistrationManager, WalletManager }

class RegistrationTests extends TestBase {

  "The registrationManager" when {
    "beginning registration" should {
      "work as expected" in {
        // retrieve possible identities
        val wallet = WalletManager.getWallet(walletPath)
        wallet should not be null

        println(s"CA-url: $caURL")
        println(tlsCert.toAbsolutePath)

        println("Enroll as admin and store cert to wallet")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, username, password, organisationId)

        println("Register TestUser")
        val testUserName = "Tester123"
        val testUserPw = RegistrationManager.register(tlsCert, caURL, testUserName, username, walletPath, organisationId, "org1")

        println("Enroll TestUser")
        EnrollmentManager.enroll(caURL, tlsCert, walletPath, testUserName, testUserPw, organisationId)

        println("Access Chain as TestUser")
        val connection = initializeCourses(testUserName)
        connection.addCourse("{\"courseId\":\"" + "1" + "\",\"courseName\":\"IQC\",\"courseType\":\"Lecture\",\"startDate\":\"1998-01-01\",\"endDate\":\"1999-01-01\",\"ects\":7,\"lecturerId\":\"Mustermann\",\"maxParticipants\":80,\"currentParticipants\":20,\"courseLanguage\":\"English\",\"courseDescription\":\"Fun new course\"}")
        val result = connection.getCourseById("1")
        println(result)
      }
    }
  }
}
