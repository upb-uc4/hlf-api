package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger

protected[hyperledger] case class ConnectionApproval(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path) extends ConnectionApprovalsTrait {
  final override val contractName: String = "UC4.Approval"
  override val (contract, gateway) = ConnectionManager.initializeConnection(username, channel, chaincode, this.contractName, walletPath, networkDescriptionPath)
  override val approvalConnection: Option[ConnectionApprovalsTrait] = None

  override def approveTransaction(contractName: String, transactionName: String, params: String*): String = {
    Logger.info(s"Approving: $contractName ::: $transactionName ::: ${params.toArray[String].foldLeft[String]("")((B,A)=> B+";"+A)}")
    wrapSubmitTransaction(false, "approveTransaction", params.prepended(transactionName).prepended(contractName): _*)
  }

  override def getApprovals(contractName: String, transactionName: String, params: String*): String =
    wrapEvaluateTransaction("getApprovals", params.prepended(transactionName).prepended(contractName): _*)
}
