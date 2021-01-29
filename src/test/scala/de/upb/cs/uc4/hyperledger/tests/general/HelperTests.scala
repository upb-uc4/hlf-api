package de.upb.cs.uc4.hyperledger.tests.general

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil.{ TestDataMatriculation, TestHelperStrings }
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, StringHelper, TransactionHelper }

class HelperTests extends TestBase {

  "The StringHelper" when {
    "parsing OperationInfo" should {
      "extract the correct operationId" in {
        val compareMat = TestDataMatriculation.validMatriculationData1("frontend-signing-tester")
        val operationInfo = "{\"operationId\":\"0rhY7SWcWIb-yjRYLMBzc3r2rZ-ar-n95Tbls6P3ClA=\",\"transactionInfo\":{\"contractName\":\"UC4.MatriculationData\",\"transactionName\":\"addMatriculationData\",\"parameters\":\"[\\\"{\\\\n \\\\\\\"enrollmentId\\\\\\\": \\\\\\\"frontend-signing-tester\\\\\\\",\\\\n \\\\\\\"matriculationStatus\\\\\\\": [\\\\n {\\\\n \\\\\\\"fieldOfStudy\\\\\\\": \\\\\\\"Computer Science\\\\\\\",\\\\n \\\\\\\"semesters\\\\\\\": [\\\\n \\\\\\\"SS2020\\\\\\\"\\\\n ]\\\\n }\\\\n ]\\\\n}\\\"]\"},\"initiator\":\"frontend-signing-tester\",\"initiatedTimestamp\":\"2021-01-19T19:02:02\",\"lastModifiedTimestamp\":\"2021-01-19T19:02:02\",\"state\":\"PENDING\",\"reason\":\"\",\"existingApprovals\":{\"users\":[\"test-admin\",\"frontend-signing-tester\"],\"groups\":[\"Admin\"]},\"missingApprovals\":{\"users\":[],\"groups\":[]}}"
        Logger.debug(operationInfo)
        val result = StringHelper.getOperationIdFromOperation(operationInfo)
        result should be("0rhY7SWcWIb-yjRYLMBzc3r2rZ-ar-n95Tbls6P3ClA=")
      }
      "extract the correct transactionInformation" in {
        val compareMat = TestDataMatriculation.validMatriculationData1("frontend-signing-tester")
        val operationInfo = "{\"operationId\":\"0rhY7SWcWIb-yjRYLMBzc3r2rZ-ar-n95Tbls6P3ClA=\",\"transactionInfo\":{\"contractName\":\"UC4.MatriculationData\",\"transactionName\":\"addMatriculationData\",\"parameters\":\"[\\\"{\\\\n \\\\\\\"enrollmentId\\\\\\\": \\\\\\\"frontend-signing-tester\\\\\\\",\\\\n \\\\\\\"matriculationStatus\\\\\\\": [\\\\n {\\\\n \\\\\\\"fieldOfStudy\\\\\\\": \\\\\\\"Computer Science\\\\\\\",\\\\n \\\\\\\"semesters\\\\\\\": [\\\\n \\\\\\\"SS2020\\\\\\\"\\\\n ]\\\\n }\\\\n ]\\\\n}\\\"]\"},\"initiator\":\"frontend-signing-tester\",\"initiatedTimestamp\":\"2021-01-19T19:02:02\",\"lastModifiedTimestamp\":\"2021-01-19T19:02:02\",\"state\":\"PENDING\",\"reason\":\"\",\"existingApprovals\":{\"users\":[\"test-admin\",\"frontend-signing-tester\"],\"groups\":[\"Admin\"]},\"missingApprovals\":{\"users\":[],\"groups\":[]}}"
        Logger.debug(operationInfo)
        val result = StringHelper.getTransactionInfoFromOperation(operationInfo)
        val (contractName, transactionName, params) = StringHelper.getInfoFromTransactionInfo(result)
        contractName should be("UC4.MatriculationData")
        transactionName should be("addMatriculationData")
        params.length should be(1)
        val retrievedParam = params.head
        retrievedParam.replace(" ", "") should equal(compareMat.replace(" ", ""))
      }
    }
    "json parsing arrays" should {
      "not change the string structure" in {
        val mat = TestDataMatriculation.validMatriculationData1("500")
        val params: Array[String] = Seq(mat).toArray

        val json: String = StringHelper.parameterArrayToJson(params)
        val resultParams = StringHelper.parameterArrayFromJson(json)

        resultParams should equal(params)
      }
    }
  }
  "The TransactionHelper " when {
    "creating a parameter list" should {
      "not change the string structure" in {
        val mat = TestDataMatriculation.validMatriculationData1("500")
        val params: Array[String] = Seq(mat).toArray

        val paramList = TransactionHelper.getApprovalParameterList("500", "UC4.MatriculationData", "addMatriculationData", params: Array[String])
        Logger.debug(paramList.toIndexedSeq.toString())
        val jSonParams: String = paramList.tail.tail.tail.head
        Logger.debug(jSonParams)
        val paramsArray = new Gson().fromJson(jSonParams, classOf[Array[String]])
        paramsArray.foreach(item => Logger.debug(item))
        mat should equal(paramsArray.head)
      }
    }
  }

  "The TestHelperStrings " when {
    "creating a parameter list" should {
      "create a valid json List null" in {
        val result = TestHelperStrings.getJsonList(null)
        result should be("[]")
      }
      "create a valid json List single" in {
        val result = TestHelperStrings.getJsonList(Seq("M1"))
        result should be("[M1]")
      }
      "create a valid json List two" in {
        val result = TestHelperStrings.getJsonList(Seq("M1", "M2"))
        result should be("[M1, M2]")
      }
    }
  }
}
