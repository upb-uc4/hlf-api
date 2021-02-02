package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path
import com.google.gson.Gson
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionOperationTrait
import de.upb.cs.uc4.hyperledger.utilities.helper.TransactionHelper

import scala.jdk.CollectionConverters.SeqHasAsJava

protected[hyperledger] case class ConnectionOperation(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionOperationTrait {

  override lazy val operationsConnection: Option[ConnectionOperationTrait] = None

  override def getProposalInitiateOperation(certificate: String, affiliation: String = AFFILIATION, initiator: String, contractName: String, transactionName: String, params: Array[String]): Array[Byte] = {
    val fcnName: String = this.contractName + ":" + "initiateOperation"
    val args: Seq[String] = TransactionHelper.getApprovalParameterList(initiator, contractName, transactionName, params)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, this.networkDescriptionPath, args: _*)
  }

  override def getProposalApproveOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String): Array[Byte] = {
    val fcnName: String = this.contractName + ":" + "approveOperation"
    val args: Seq[String] = Seq(operationId)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, networkDescriptionPath, args: _*)
  }

  override def getProposalRejectOperation(certificate: String, affiliation: String = AFFILIATION, operationId: String, rejectMessage: String): Array[Byte] = {
    val fcnName = this.contractName + ":" + "rejectOperation"
    val args: Seq[String] = Seq(operationId, rejectMessage)
    TransactionHelper.createProposal(certificate, affiliation, chaincode, channel, fcnName, networkDescriptionPath, args: _*)
  }

  override def initiateOperation(initiator: String, contractName: String, transactionName: String, params: String*): String = {
    val transactionValues = TransactionHelper.getApprovalParameterList(initiator, contractName, transactionName, params.toArray)
    wrapSubmitTransaction(false, "initiateOperation", transactionValues: _*)()
  }

  override def approveOperation(operationId: String): String = {
    wrapSubmitTransaction(false, "approveOperation", operationId)()
  }

  override def rejectOperation(operationId: String, rejectMessage: String): String = {
    wrapSubmitTransaction(false, "rejectOperation", operationId, rejectMessage)()
  }

  override def getOperations(operationIds: List[String], existingEnrollmentId: String, missingEnrollmentId: String, initiatorEnrollmentId: String, involvedEnrollmentId: String, states: List[String]): String = {
    wrapEvaluateTransaction("getOperations",
      new Gson().toJson(operationIds.asJava),
      existingEnrollmentId,
      missingEnrollmentId,
      initiatorEnrollmentId,
      involvedEnrollmentId,
      new Gson().toJson(states.asJava))
  }
}
