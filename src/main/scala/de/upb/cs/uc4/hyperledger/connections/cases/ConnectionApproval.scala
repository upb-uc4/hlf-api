package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, TransactionHelper }

protected[hyperledger] case class ConnectionApproval(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionApprovalsTrait {
  final override val contractName: String = "UC4.Approval"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)
  override val approvalConnection: Option[ConnectionApprovalsTrait] = None

  override def approveTransaction(contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalTransactionFromParameters(contractName, transactionName, params: _*)
    wrapSubmitTransaction(false, "approveTransaction", transactionValues: _*)
  }

  override def getApprovals(contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalTransactionFromParameters(contractName, transactionName, params: _*)
    wrapEvaluateTransaction("getApprovals", transactionValues: _*)
  }

}
