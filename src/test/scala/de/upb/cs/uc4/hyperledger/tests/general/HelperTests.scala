package de.upb.cs.uc4.hyperledger.tests.general

import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, TransactionHelper }

class HelperTests extends TestBase {

  "The TransactionHelper" when {
    "parsing OperationInfo" should {
      "extract the correct information" in {
        val operationInfo = "{\"operationId\":\"0rhY7SWcWIb-yjRYLMBzc3r2rZ-ar-n95Tbls6P3ClA=\",\"transactionInfo\":{\"contractName\":\"UC4.MatriculationData\",\"transactionName\":\"addMatriculationData\",\"parameters\":\"[\\\"{\\\\n \\\\\\\"enrollmentId\\\\\\\": \\\\\\\"frontend-signing-tester\\\\\\\",\\\\n \\\\\\\"matriculationStatus\\\\\\\": [\\\\n {\\\\n \\\\\\\"fieldOfStudy\\\\\\\": \\\\\\\"Computer Science\\\\\\\",\\\\n \\\\\\\"semesters\\\\\\\": [\\\\n \\\\\\\"SS2020\\\\\\\"\\\\n ]\\\\n }\\\\n ]\\\\n}\\\"]\"},\"initiator\":\"frontend-signing-tester\",\"initiatedTimestamp\":\"2021-01-19T19:02:02\",\"lastModifiedTimestamp\":\"2021-01-19T19:02:02\",\"state\":\"PENDING\",\"reason\":\"\",\"existingApprovals\":{\"users\":[\"test-admin\",\"frontend-signing-tester\"],\"groups\":[\"Admin\"]},\"missingApprovals\":{\"users\":[],\"groups\":[]}}"
        val result = TransactionHelper.getTransactionInfoFromOperation(operationInfo)
        val r = TransactionHelper.getInfoFromTransactionInfo(result)
        r._1 should be("UC4.MatriculationData")
        r._2 should be("addMatriculationData")
        r._3.length should be(1)
      }
    }
  }
}
