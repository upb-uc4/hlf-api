package de.upb.cs.uc4.hyperledger.tests

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionExaminationRegulationTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataExaminationRegulation, TestHelper }

class ExaminationRegulationErrorTests extends TestBase {

  var chaincodeConnection: ConnectionExaminationRegulationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeExaminationRegulation()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  def validTestModule(id: String): String = TestDataExaminationRegulation.getModule(id, "testName")

  "The ScalaAPI for ExaminationRegulations" when {
    "invoked with addExaminationRegulation incorrectly " should {
      val testData: Seq[(String, String, Array[String], Boolean)] = Seq(
        ("throw an exception for ExaminationRegulations with empty Modules", "001", new Array[String](0), true),
        ("throw an exception for ExaminationRegulations with empty Modules", "001", new Array[String](0), false),
        ("throw an exception for ExaminationRegulations with null Modules", "001", null, true),
        ("throw an exception for ExaminationRegulations with null Modules", "001", null, false),
        ("throw an exception for ExaminationRegulations with illegal Name", "000", Array(validTestModule("1")), true),
        ("throw an exception for ExaminationRegulations with illegal Name", "000", Array(validTestModule("1")), false),
        ("throw an exception for ExaminationRegulations with empty Name", "", Array(validTestModule("1")), true),
        ("throw an exception for ExaminationRegulations with empty Name", "", Array(validTestModule("1")), false),
        ("throw an exception for ExaminationRegulations with null Name", null, Array(validTestModule("1")), true),
        ("throw an exception for ExaminationRegulations with null Name", null, Array(validTestModule("1")), false),
        ("throw an exception for ExaminationRegulations with null open state", "001", Array(validTestModule("1")), null),
      )
      for ((testDescription: String, name: String, modules: Array[String], open: Boolean) <- testData) {
        s"$testDescription [$name][${TestHelper.nullableSeqToString(modules)}][$open]" in {
          val examinationRegulation = TestDataExaminationRegulation.validExaminationRegulation(name, modules, open)
          TestHelper.testTransactionException(
            "addExaminationRegulation",
            () => chaincodeConnection.addExaminationRegulation(examinationRegulation))
        }
      }
    }
    "invoked with getExaminationRegulations incorrectly " should {
      val testData: Seq[(String, Seq[String])] = Seq(
        ("throw TransactionException for not existing examinationRegulationName", Seq("010")),
        ("throw TransactionException for containing not existing examinationRegulationName", Seq("001", "010")),
        ("throw TransactionException for containing null existing examinationRegulationName", Seq("001", null)),
        ("throw TransactionException for containing null existing examinationRegulationName", Seq(null, "001")),
        ("throw TransactionException for containing null existing examinationRegulationName", Seq(null, "001", null))
      )
      for ((statement: String, names: Seq[String]) <- testData) {
        s"$statement [${TestHelper.nullableSeqToString(names)}]" in {
          val namesList = TestHelper.getJsonList(names)
          TestHelper.testTransactionException("getExaminationRegulations", () => chaincodeConnection.getExaminationRegulations(namesList))
        }
      }
    }
    "invoked with closeExaminationRegulation incorrectly " should {
      val testData: Seq[(String, String)] = Seq(
        ("throw TransactionException for not existing examinationRegulationName ", "010"),
        ("throw TransactionException for empty examinationRegulationName ", ""),
        ("throw TransactionException for null examinationRegulationName ", null),
      )
      for ((statement: String, name: String) <- testData) {
        s"$statement [$name]" in {
          TestHelper.testTransactionException("closeExaminationRegulation", () => chaincodeConnection.closeExaminationRegulation(name))
        }
      }
    }
  }
}