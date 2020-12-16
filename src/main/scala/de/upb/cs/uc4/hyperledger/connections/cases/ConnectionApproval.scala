package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.TransactionHelper

protected[hyperledger] case class ConnectionApproval(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionApprovalsTrait {

  override def approvalConnection: Option[ConnectionApprovalsTrait] = None

  override def approveTransaction(contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalParameterList(contractName, transactionName, params.toArray)
    wrapSubmitTransaction(false, "approveTransaction", transactionValues: _*)
  }

  override def getApprovals(contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalParameterList(contractName, transactionName, params.toArray)
    wrapEvaluateTransaction("getApprovals", transactionValues: _*)
  }
}
