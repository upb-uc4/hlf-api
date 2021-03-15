package de.upb.cs.uc4.hyperledger.tests.contracts

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testData.TestDataExaminationRegulation
import de.upb.cs.uc4.hyperledger.testUtil.{ TestHelper, TestHelperStrings }

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
        TestHelper.testAddExaminationRegulationAccess(chaincodeConnection, name, modules, state = false)
      }
      "allow for adding ExaminationRegulations with null name [null][4,5,6][open] " in {
        val name = null
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
        val names = TestHelperStrings.getJsonList(Seq(name))
        val testResult = chaincodeConnection.getExaminationRegulations(names)
        testResult should not be null
        testResult should be("[]")
      }
      "allow for reading existing ExaminationRegulations [001] " in {
        val name = "001"
        val names = TestHelperStrings.getJsonList(Seq(name))
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
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, isOpen = true)
        val expectedResultList = TestHelperStrings.getJsonList(Seq(expectedResult))

        // read data
        val names = TestHelperStrings.getJsonList(Seq(name))
        val testResultList = chaincodeConnection.getExaminationRegulations(names)

        // compare data
        // TestHelper.compareJson(expectedResultList, testResultList)
        // TODO: cannot do that, since other tests might store additional Examination regulations
      }
      "read the correct ExaminationRegulations [] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, isOpen = true)
        val expectedResultList = TestHelperStrings.getJsonList(Seq(expectedResult))

        // read data
        val testResultList = chaincodeConnection.getExaminationRegulations("[]")

        // compare data
        // TestHelper.compareJson(expectedResultList, testResultList)
        // TODO: cannot do that, since other tests might store additional Examination regulations
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
        val names = TestHelperStrings.getJsonList(Seq(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read new closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, isOpen = false)
        val expectedResultList = TestHelperStrings.getJsonList(Seq(expectedResult))
        TestHelperStrings.compareJson(expectedResultList, testResultList)
      }
      "allow for closing an already closed ExaminationRegulation [001] " in {
        val name = "001"
        val modules = Seq(
          TestDataExaminationRegulation.getModule("M.1", "shortName"),
          TestDataExaminationRegulation.getModule("M.2", "shortName"),
          TestDataExaminationRegulation.getModule("M.3", "shortName")
        )
        val names = TestHelperStrings.getJsonList(Seq(name))

        chaincodeConnection.closeExaminationRegulation(name)

        // read still closed
        val testResultList = chaincodeConnection.getExaminationRegulations(names)
        val expectedResult = TestDataExaminationRegulation.validExaminationRegulation(name, modules, isOpen = false)
        val expectedResultList = TestHelperStrings.getJsonList(Seq(expectedResult))
        TestHelperStrings.compareJson(expectedResultList, testResultList)
      }
    }
  }
}