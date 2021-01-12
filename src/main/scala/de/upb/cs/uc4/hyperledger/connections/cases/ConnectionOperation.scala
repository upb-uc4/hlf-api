package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionOperationsTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.TransactionHelper

protected[hyperledger] case class ConnectionOperation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionOperationsTrait {

  override lazy val operationsConnection: Option[ConnectionOperationsTrait] = None

  override def approveTransaction(initiator: String, contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalParameterList(initiator, contractName, transactionName, params.toArray)
    wrapSubmitTransaction(false, "approveTransaction", transactionValues: _*)
  }

  override def rejectTransaction(operationId: String, rejectMessage: String): String = {
    wrapSubmitTransaction(false, "rejectTransaction", operationId, rejectMessage)
  }

  override def getOperations(existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, state: String): String = {
    wrapSubmitTransaction(false, "getOperations", existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, state: String)
  }

  override def getOperationData(operationId: String): String = {
    wrapSubmitTransaction(false, "getOperationData", operationId)
  }
}
