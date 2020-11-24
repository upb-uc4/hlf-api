package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataExaminationRegulation, TestHelper }

class ExaminationRegulationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionExaminationRegulationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    chaincodeConnection = initializeExaminationRegulation()
  }

  override def afterEach(): Unit = {
    chaincodeConnection.close()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for ExaminationRegulations" when {
    "invoked with correct transactions " should {
      "allow for adding new ExaminationRegulations [001][1,2,3][true] " in {
        val name = "001"
        val modules = Array(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        TestHelper.testAddExaminationRegulationAccess(chaincodeConnection, name, modules, true)
      }
      "allow for adding new ExaminationRegulations [002][4,5,6][false] " in {
        val name = "002"
        val modules = Array(
          TestDataExaminationRegulation.getModule("M.4", "shortName"),
          TestDataExaminationRegulation.getModule("M.5", "shortName"),
          TestDataExaminationRegulation.getModule("M.6", "shortName")
        )
        TestHelper.testAddExaminationRegulationAccess(chaincodeConnection, name, modules, true)
      }
      "allow for reading ExaminationRegulations [001] " in {
        val name = "001"
        val names = TestHelper.getJsonList(Array(name))
        val testResult = chaincodeConnection.getExaminationRegulations(names)
        testResult should not be (null)
      }
      "read the correct ExaminationRegulations [001] " in {
        val name = "001"
        val modules = Array(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Array(name))

        // read and compare data
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, true)
        val expectedResultList = TestHelper.getJsonList(Array(expectedResult))
        TestHelper.compareJson(testResultList, expectedResultList)
      }
      "allow for closing an ExaminationRegulation [001] " in {
        val name = "001"
        val modules = Array(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Array(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read new closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, false)
        val expectedResultList = TestHelper.getJsonList(Array(expectedResult))
        TestHelper.compareJson(expectedResultList, testResultList)
      }
      "allow for closing an already closed ExaminationRegulation [001] " in {
        val name = "001"
        val modules = Array(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Array(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read still closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, false)
        val expectedResultList = TestHelper.getJsonList(Array(expectedResult))
        TestHelper.compareJson(expectedResultList, testResultList)
      }
    }
  }
}