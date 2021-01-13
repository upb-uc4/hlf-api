package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionOperationsTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.TransactionHelper

protected[hyperledger] case class ConnectionOperation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionOperationsTrait {

  override lazy val operationsConnection: Option[ConnectionOperationsTrait] = None

  override def getProposalProposeTransaction(certificate: String, affiliation: String = AFFILIATION, initiator: String, contractName: String, transactionName: String, params: Array[String]): Array[Byte] = {
    val fcnName: String = contractName + ":" + "proposeTransaction"
    val args: Seq[String] = TransactionHelper.getApprovalParameterList(initiator, this.contractName, transactionName, params)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, this.networkDescriptionPath, args: _*)
  }

  override def getProposalApproveOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String): Array[Byte] = {
    val fcnName: String = contractName + ":" + "approveOperation"
    val args: Seq[String] = Seq(operationId)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, networkDescriptionPath, args: _*)
  }

  override def getProposalRejectOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String, rejectMessage: String): Array[Byte] = {
    val fcnName = contractName + ":" + "rejectOperation"
    val args: Seq[String] = Seq(operationId, rejectMessage)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, networkDescriptionPath, args: _*)
  }

  override def proposeTransaction(initiator: String, contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalParameterList(initiator, contractName, transactionName, params.toArray)
    wrapSubmitTransaction(false, "proposeTransaction", transactionValues: _*)
  }

  override def approveOperation(operationId: String): String = {
    wrapSubmitTransaction(false, "approveOperation", operationId)
  }

  override def rejectOperation(operationId: String, rejectMessage: String): String = {
    wrapSubmitTransaction(false, "rejectTransaction", operationId, rejectMessage)
  }

  override def getOperations(operationIds: List[String], existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, involvedEnrollmentId: String, states: List[String]): String = {
    wrapSubmitTransaction(false, "getOperations",
      new Gson().toJson(operationIds),
      existingEnrollmentId,
      missingEnrollmentId,
      initiatorEnrollmentId,
      involvedEnrollmentId,
      new Gson().toJson(states))
  }
}
