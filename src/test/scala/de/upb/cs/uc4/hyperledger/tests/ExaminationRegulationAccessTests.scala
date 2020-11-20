package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataExaminationRegulation, TestHelper }

class ExaminationRegulationAccessTests extends TestBase {

  var chaincodeConnection: ConnectionExaminationRegulationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeExaminationRegulation()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ScalaAPI for ExaminationRegulations" when {
    "invoked with addExaminationRegulation correctly " should {
      "allow for adding open ExaminationRegulations [001][1,2,3][true] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        TestHelper.testAddExaminationRegulationAccess(chaincodeConnection, name, modules, state = true)
      }
      "allow for adding closed ExaminationRegulations [002][4,5,6][false] " in {
        val name = "002"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.4", "shortName"),
          TestDataExaminationRegulation.getModule("M.5", "shortName"),
          TestDataExaminationRegulation.getModule("M.6", "shortName")
        )
        TestHelper.testAddExaminationRegulationAccess(chaincodeConnection, name, modules, state = true)
      }
    }
    "invoked with getExaminationRegulations correctly " should {
      "allow for reading non existing ExaminationRegulations [010] " in {
        val name = "010"
        val names = TestHelper.getJsonList(Seq(name))
        val testResult = chaincodeConnection.getExaminationRegulations(names)
        testResult should not be null
        testResult should be("[]")
      }
      "allow for reading existing ExaminationRegulations [001] " in {
        val name = "001"
        val names = TestHelper.getJsonList(Seq(name))
        val testResult = chaincodeConnection.getExaminationRegulations(names)
        testResult should not be null
      }
      "read the correct ExaminationRegulations [001] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Seq(name))

        // read and compare data
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state = true)
        val expectedResultList = TestHelper.getJsonList(Seq(expectedResult))
        TestHelper.compareJson(testResultList, expectedResultList)
      }
      "read the correct ExaminationRegulations [] " in {
        val name = ""
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Seq(name))

        // read and compare data
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state = true)
        val expectedResultList = TestHelper.getJsonList(Seq(expectedResult))
        TestHelper.compareJson(testResultList, expectedResultList)
      }
    }
    "invoked with closeExaminationRegulation correctly " should {
      "allow for closing an ExaminationRegulation [001] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Seq(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read new closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state = false)
        val expectedResultList = TestHelper.getJsonList(Seq(expectedResult))
        TestHelper.compareJson(expectedResultList, testResultList)
      }
      "allow for closing an already closed ExaminationRegulation [001] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelper.getJsonList(Seq(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read still closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, state = false)
        val expectedResultList = TestHelper.getJsonList(Seq(expectedResult))
        TestHelper.compareJson(expectedResultList, testResultList)
      }
    }
  }
}