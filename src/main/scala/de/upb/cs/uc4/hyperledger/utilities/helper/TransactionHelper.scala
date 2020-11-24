package de.upb.cs.uc4.hyperledger.utilities.helper

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.security.PrivateKey

import com.google.gson.Gson
import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionApprovalsTrait
import org.hyperledger.fabric.gateway.Identities
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser
import org.hyperledger.fabric.gateway.impl.{ContractImpl, TransactionImpl}
import org.hyperledger.fabric.protos.common.Common
import org.hyperledger.fabric.protos.peer.{Chaincode, ProposalPackage}
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ChaincodeProposalPayload, Proposal, SignedProposal}
import org.hyperledger.fabric.sdk.{Channel, HFClient, NetworkConfig, TransactionProposalRequest, User}
import org.hyperledger.fabric.sdk.identity.X509Enrollment
import org.hyperledger.fabric.sdk.security.CryptoPrimitives
import org.hyperledger.fabric.sdk.transaction.{ProposalBuilder, TransactionContext}

import scala.jdk.CollectionConverters._

protected[hyperledger] object TransactionHelper {

  def getParametersFromApprovalProposal(proposal: Proposal): (String, String, Array[String]) = {
    // read transaction info
    val proposalParameters = TransactionHelper.getTransactionParamsFromProposal(proposal)
    val proposalContractName = proposalParameters.head
    val transactionName = proposalParameters.tail.head
    val paramsGson = proposalParameters.tail.tail.head
    println("GSON:::: " + paramsGson)
    val params = new Gson().fromJson[Array[String]](paramsGson, classOf[Array[String]])
    (proposalContractName, transactionName, params)
  }

  def getApprovalTransactionFromParameters(contractName: String, transactionName: String, params: Array[String]): Seq[String] = {
    val jsonParams = new Gson().toJson(params)
    val info = List[String](contractName, transactionName, jsonParams)
    Logger.info(s"approval info: ${info.foldLeft("")((A, B) => A + "::" + B)}")
    info
  }

  def createApprovalTransactionInfo(approvalContract: ContractImpl, contractName: String, transactionName: String, params: Array[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val approvalParams: Seq[String] = getApprovalTransactionFromParameters(contractName, transactionName, params)
    createTransactionInfo(approvalContract, "approveTransaction", approvalParams.toArray, transactionId)
  }

  def createTransactionInfo(contract: ContractImpl, transactionName: String, params: Array[String], transactionId: Option[String]): (TransactionImpl, TransactionContext, TransactionProposalRequest) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.safeCallPrivateMethod(transaction)("newProposalRequest")(params).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = request.getTransactionContext.get()
    if (transactionId.isDefined) ReflectionHelper.setPrivateField(context)("txID")(transactionId.get)
    if (request.getTransientMap != null) transaction.setTransient(request.getTransientMap)
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
    val args: Array[ByteString] = chaincodeInput.getArgsList.asScala.toArray
    args.map[String]((b: ByteString) => new String(b.toByteArray, StandardCharsets.UTF_8)).toList
  }

  def createSignedProposal(approvalConnection: ConnectionApprovalsTrait, proposal: ProposalPackage.Proposal, signature: ByteString): (TransactionImpl, TransactionContext, SignedProposal) = {
    val transactionId: String = TransactionHelper.getTransactionIdFromProposal(proposal)
    val transactionName: String = TransactionHelper.getTransactionNameFromProposal(proposal)
    val params: Seq[String] = TransactionHelper.getTransactionParamsFromProposal(proposal)

    val signedProposalBuilder: SignedProposal.Builder = SignedProposal.newBuilder
      .setProposalBytes(proposal.toByteString)
      .setSignature(signature)
    val signedProposal: SignedProposal = signedProposalBuilder.build

    val (transaction, context, request) = TransactionHelper.createTransactionInfo(approvalConnection.contract, transactionName, params.toArray, Some(transactionId))

    (transaction, context, signedProposal)
  }

  def getUnsignedProposalNew(
                              certificate: String,
                              userAffiliation: String,
                              chaincodeName: String,
                              channelName: String,
                              function: String,
                              networkDescriptionPath: Path,
                              args: String*): Proposal = {
    val enrollment: X509Enrollment = new X509Enrollment(new PrivateKey {
      override def getAlgorithm: String = null
      override def getFormat: String = null
      override def getEncoded: Array[Byte] = null
    }, certificate)
    val user: User = new GatewayUser("gateway", userAffiliation, enrollment);
    // val user: User = new GatewayUser(argEnrollmentId, testAffiliation, new X509Enrollment(adminIdentity.getPrivateKey, Identities.toPemString(adminIdentity.getCertificate)))
    val request = TransactionProposalRequest.newInstance(user)
    request.setChaincodeName(chaincodeName)
    request.setFcn(function)
    request.setArgs(args: _*)
    val networkConfigFile: File = networkDescriptionPath.toFile()
    val networkConfig: NetworkConfig = NetworkConfig.fromYamlFile(networkConfigFile)
    val hfClient: HFClient = HFClient.createNewInstance()
    val crypto: CryptoPrimitives = new CryptoPrimitives()
    val securityLevel: Integer = 256
    ReflectionHelper.safeCallPrivateMethod(crypto)("setSecurityLevel")(securityLevel)
    // TODO use get- and set properties
    hfClient.setCryptoSuite(crypto)
    hfClient.setUserContext(user)
    val channelObj: Channel = hfClient.loadChannelFromConfig(channelName, networkConfig)
    val ctx: TransactionContext = new TransactionContext(channelObj, user, crypto)
    val chaincodeId: Chaincode.ChaincodeID = Chaincode.ChaincodeID.newBuilder().setName(chaincodeName).build()
    ProposalBuilder.newBuilder().context(ctx).request(request).chaincodeID(chaincodeId).build()
  }

}
