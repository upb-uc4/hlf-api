package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util
import java.util.concurrent.TimeoutException

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionApproval
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, ReflectionHelper, TransactionHelper }
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.gateway.GatewayRuntimeException
import org.hyperledger.fabric.protos.peer.ProposalPackage
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ Proposal, SignedProposal }
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionContext }

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  // regular info used to set up any connection
  val username: String
  val channel: String
  val chaincode: String
  val walletPath: Path
  val networkDescriptionPath: Path

  // contract info for specific connections
  val contractName: String

  // setting up connections
  lazy val (contract: ContractImpl, gateway: GatewayImpl) = ConnectionManager.initializeConnection(username, channel, chaincode, contractName, walletPath, networkDescriptionPath)
  lazy val approvalConnection: Option[ConnectionApprovalsTrait] = Some(ConnectionApproval(username, channel, chaincode, walletPath, networkDescriptionPath))

  /** Gets the version returned by the designated contract.
    * By default all contracts return the version of the chaincode.
    *
    * @return String containing versionInfo
    */
  final def getChaincodeVersion: String = wrapEvaluateTransaction("getVersion")

  /** Wrapper for a submission transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transient        boolean flag to determine transaction to be transient or not.
    * @param transactionName  transaction to call
    * @param params           parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapSubmitTransaction(transient: Boolean, transactionName: String, params: String*): String = {
    // submit my approval to approvalContract
    if (approvalConnection.isDefined) approvalConnection.get.approveTransaction(contractName, transactionName, params: _*)

    // submit and evaluate response from my "regular" contract
    val result = this.privateSubmitTransaction(transient, transactionName, params: _*)
    this.wrapTransactionResult(transactionName, result)
  }

  /** Wrapper for an evaluation transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transactionName transaction to call
    * @param params        parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapEvaluateTransaction(transactionName: String, params: String*): String = {
    // submit my approval to approvalContract
    if (approvalConnection.isDefined) approvalConnection.get.approveTransaction(contractName, transactionName, params: _*)

    val result = this.privateEvaluateTransaction(transactionName, params: _*)
    this.wrapTransactionResult(transactionName, result)
  }

  protected final def internalGetUnsignedProposal(transactionName: String, params: String*): Array[Byte] = {
    // send approval as admin maintaining the connection
    approvalConnection.get.approveTransaction(contractName, "addCertificate", params: _*)

    // gather info
    val (transaction, context, request) = TransactionHelper.createApprovalTransactionInfo(approvalConnection.get.contract, contractName, transactionName, params.toArray, None)

    // create proposal
    val proposal: Proposal = ProposalBuilder.newBuilder().request(request).context(context).build()
    proposal.toByteArray
  }

  def submitSignedProposal(proposalBytes: Array[Byte], signatureBytes: Array[Byte]): String = {
    val proposal: Proposal = Proposal.parseFrom(proposalBytes)
    val signature: ByteString = ByteString.copyFrom(signatureBytes)

    val (transaction: TransactionImpl, context: TransactionContext, signedProposal: SignedProposal) = this.createSignedProposal(proposal, signature)

    // submit approval
    val approvalResult = this.internalSubmitApprovalProposal(transaction, context, signedProposal)
    var transactionResult = approvalResult
    try {
      // submit real transaction as admin
      transactionResult = this.internalSubmitRealTransactionFromApprovalProposal(proposal)
    }
    catch {
      case e: TransactionExceptionTrait => throw e
    }
    transactionResult
  }

  def internalSubmitRealTransactionFromApprovalProposal(proposal: Proposal): String = {
    val (proposalContractName, transactionName, params) = TransactionHelper.getParametersFromApprovalProposal(proposal)

    // Logging
    Logger.warn("contractName" + proposalContractName)
    Logger.warn("transactionName" + transactionName)
    Logger.warn("params" + params.mkString(";"))

    // check contract match
    if (proposalContractName != contractName) throw TransactionException.CreateUnknownException("approveTransaction", s"Approval was sent to wrong connection:: $contractName != $proposalContractName")

    // submit and evaluate response from my "regular" contract
    // TODO: pass transient bool
    val result = this.privateSubmitTransaction(false, transactionName, params.toIndexedSeq: _*)
    this.wrapTransactionResult(transactionName, result)
  }

  def internalSubmitApprovalProposal(transaction: TransactionImpl, context: TransactionContext, signedProposal: SignedProposal): String = {
    val transactionName = TransactionHelper.getTransactionNameFromFcn(transaction.getName)
    if (transactionName != "approveTransaction") throw new Exception("submitSigned Proposal was invoked with a non approval transaction.")
    val proposalResponses = this.sendProposalToPeers(context, signedProposal)

    // evaluate proposal
    try {
      val validResponses = ReflectionHelper.safeCallPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]
      val result = ReflectionHelper.safeCallPrivateMethod(transaction)("commitTransaction")(validResponses).asInstanceOf[Array[Byte]]
      this.wrapTransactionResult(transactionName, result)
    }
    catch {
      case ex: HyperledgerExceptionTrait => throw HyperledgerException(transactionName, ex)
    }
  }

  private def createSignedProposal(proposal: ProposalPackage.Proposal, signature: ByteString): (TransactionImpl, TransactionContext, SignedProposal) = {
    val transactionId: String = TransactionHelper.getTransactionIdFromProposal(proposal)
    val transactionName: String = TransactionHelper.getTransactionNameFromProposal(proposal)
    val params: Seq[String] = TransactionHelper.getTransactionParamsFromProposal(proposal)

    val signedProposalBuilder: SignedProposal.Builder = SignedProposal.newBuilder
      .setProposalBytes(proposal.toByteString)
      .setSignature(signature)
    val signedProposal: SignedProposal = signedProposalBuilder.build

    val (transaction, context, request) = TransactionHelper.createTransactionInfo(approvalConnection.get.contract, transactionName, params.toArray, Some(transactionId))

    (transaction, context, signedProposal)
  }

  private def sendProposalToPeers(context: TransactionContext, signedProposal: ProposalPackage.SignedProposal) = {
    val channel: Channel = contract.getNetwork.getChannel
    val peers: util.Collection[Peer] = ReflectionHelper.safeCallPrivateMethod(channel)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    ReflectionHelper.safeCallPrivateMethod(channel)("sendProposalToPeers")(peers, signedProposal, context).asInstanceOf[util.Collection[ProposalResponse]]
  }

  @throws[HyperledgerExceptionTrait]
  private def privateSubmitTransaction(transient: Boolean, transactionName: String, params: String*): Array[Byte] = {
    testAnyParamsNull(transactionName, params: _*)
    try {
      if (transient) {
        var i = 0
        var transMap: Map[String, Array[Byte]] = Map()
        params.foreach(param => {
          transMap += i.toString -> param.toCharArray.map(_.toByte)
          i = i + 1
        })

        // TODO: once transient is reenabled, test this
        // var i = 0
        // val transMap2 = params.toList.map((entry: String) =>
        // ((i+=1).toString -> entry.toCharArray.map(_.toByte)))

        contract.createTransaction(transactionName).setTransient(transMap.asJava).submit()
      }
      else {
        contract.submitTransaction(transactionName, params: _*)
      }
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionName, ex)
    }
  }

  @throws[HyperledgerExceptionTrait]
  private def privateEvaluateTransaction(transactionName: String, params: String*): Array[Byte] = {
    testAnyParamsNull(transactionName, params: _*)
    try {
      contract.evaluateTransaction(transactionName, params: _*)
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionName, ex)
    }
  }

  /** Wraps the chaincode query result bytes.
    * Translates the byte-array to a string and throws an error if said string contains an error.
    *
    * @param result input byte-array to translate
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  private def wrapTransactionResult(transactionName: String, result: Array[Byte]): String = {
    val resultString = new String(result, StandardCharsets.UTF_8)
    if (containsError(resultString)) throw TransactionException(transactionName, resultString)
    else resultString
  }

  /** Evaluates whether a transactionResult contains a "detailedError" or a "genericError"
    *
    * @param result result of a chaincode transaction
    * @return true if the result contains error information conforming to API-standards
    */
  private def containsError(result: String): Boolean = result.contains("{\"type\":") && result.contains("\"title\":")

  final override def close(): Unit = this.gateway.close()

  /** Checks if the transaction params are null.
    *
    * @param transactionName transactionId causing the error.
    * @param params          parameters to check
    * @throws TransactionException if a parameter is null.
    */
  @throws[TransactionExceptionTrait]
  private def testAnyParamsNull(transactionName: String, params: String*): Unit = {
    if (params.exists(a => a == null)) throw TransactionException.CreateUnknownException(transactionName, "A parameter was null.")
  }
}
