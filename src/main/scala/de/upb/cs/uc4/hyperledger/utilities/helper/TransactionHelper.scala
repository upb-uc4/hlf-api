package de.upb.cs.uc4.hyperledger.utilities.helper

import java.nio.charset.StandardCharsets
import java.util

import com.google.protobuf.ByteString
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, TransactionImpl }
import org.hyperledger.fabric.protos.common.Common
import org.hyperledger.fabric.protos.peer.{ Chaincode, ProposalPackage }
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ ChaincodeProposalPayload, Proposal }
import org.hyperledger.fabric.sdk.TransactionProposalRequest
import org.hyperledger.fabric.sdk.transaction.TransactionContext

import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.jdk.CollectionConverters._


protected[hyperledger] object TransactionHelper {

  def createTransactionInfo(contract: ContractImpl, transactionName: String, params: Array[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.safeCallPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = request.getTransactionContext.get()
    if (transactionId.isDefined) ReflectionHelper.setPrivateField(context)("txID")(transactionId.get)
    context.verify(request.doVerify())
    context.setProposalWaitTime(request.getProposalWaitTime)
    ReflectionHelper.setPrivateField(transaction)("transactionContext")(context)
    (transaction, context, request)
  }

  def getTransactionIdFromProposal(proposal: Proposal): String = {
    val header = Common.Header.parseFrom(proposal.getHeader)
    val channelHeader = Common.ChannelHeader.parseFrom(header.getChannelHeader)
    val transactionId = channelHeader.getTxId
    transactionId
  }

  def getTransactionNameFromProposal(proposal: Proposal): String = {
    val args = getArgsFromProposal(proposal)
    val fcnName: String = args.head
    getTransactionNameFromFcn(fcnName)
  }

  def getTransactionParamsFromProposal(proposal: Proposal): Seq[String] = {
    val args = getArgsFromProposal(proposal)
    val params = args.tail
    params
  }

  def getTransactionNameFromFcn(fcn: String): String = fcn.substring(fcn.indexOf(":") + 1)

  private def getArgsFromProposal(proposal: Proposal): Seq[String] = {
    val payloadBytes: ByteString = proposal.getPayload
    val payload: ChaincodeProposalPayload = ProposalPackage.ChaincodeProposalPayload.parseFrom(payloadBytes)
    val invocationSpec: Chaincode.ChaincodeInvocationSpec = Chaincode.ChaincodeInvocationSpec.parseFrom(payload.getInput)
    val chaincodeInput = invocationSpec.getChaincodeSpec.getInput
    val args: util.List[ByteString] = chaincodeInput.getArgsList
    args.iterator().map[String]((b: ByteString) => new String(b.toByteArray, StandardCharsets.UTF_8)).toList
  }

}
