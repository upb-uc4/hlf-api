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
      val testData: Seq[(String, Option[String], Option[Seq[String]], Boolean)] = Seq(
        ("throw an exception for ExaminationRegulations with empty Modules", Some("001"), Some(Seq()), true),
        ("throw an exception for ExaminationRegulations with empty Modules", Some("001"), Some(Seq()), false),
        ("throw an exception for ExaminationRegulations with null Modules", Some("001"), None, true),
        ("throw an exception for ExaminationRegulations with null Modules", Some("001"), None, false),
        ("throw an exception for ExaminationRegulations with empty Name", Some(""), Some(Seq(validTestModule("1"))), true),
        ("throw an exception for ExaminationRegulations with empty Name", Some(""), Some(Seq(validTestModule("1"))), false),
        ("throw an exception for ExaminationRegulations with null Name", None, Some(Seq(validTestModule("1"))), true),
        ("throw an exception for ExaminationRegulations with null Name", None, Some(Seq(validTestModule("1"))), false),
      )
      for ((testDescription: String, name: Option[String], modules: Option[Seq[String]], open: Boolean) <- testData) {
        s"$testDescription [${name.orNull}][${TestHelper.nullableSeqToString(modules.orNull)}][$open]" in {
          val examinationRegulation: String = TestDataExaminationRegulation.validExaminationRegulation(name.orNull, modules.orNull, open)
          TestHelper.testTransactionException(
            "addExaminationRegulation",
            () => chaincodeConnection.addExaminationRegulation(examinationRegulation)
          )
        }
      }
    }
    "invoked with getExaminationRegulations incorrectly " should {
      val testData: Seq[(String, String)] = Seq(
        ("throw TransactionException for malformed examinationRegulationNamesList", "[001,]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[001, ]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[,001]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[,001,]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[,001, ]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[ ,001]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[ ,001,]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[ ,001, ]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "[001"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "001]"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "001"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "(001)"),
        ("throw TransactionException for malformed examinationRegulationNamesList", "{001}")
      )
      for ((statement: String, namesString: String) <- testData) {
        s"$statement [$namesString]" in {
          TestHelper.testTransactionException("getExaminationRegulations", () => chaincodeConnection.getExaminationRegulations(namesString))
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