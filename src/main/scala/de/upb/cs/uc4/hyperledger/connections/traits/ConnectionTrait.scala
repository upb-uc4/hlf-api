package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.util
import java.util.Optional
import java.util.concurrent.TimeoutException

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.helper.{ ReflectionHelper, TransactionHelper }
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.gateway.GatewayRuntimeException
import org.hyperledger.fabric.protos.peer.{ Chaincode, ProposalPackage }
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ Proposal, SignedProposal }
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionContext }

import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  // regular contract info
  val contractName: String
  val contract: ContractImpl
  val gateway: GatewayImpl

  // draftContract info
  val draftContractName: String = "UC4.Draft"
  val draftContract: ContractImpl
  val draftGateway: GatewayImpl

  /** Gets the version returned by the designated contract.
    * By default all contracts return the version of the chaincode.
    *
    * @return String containing versionInfo
    */
  final def getChaincodeVersion: String = wrapEvaluateTransaction("getVersion")

  /** Wrapper for a submission transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transient     boolean flag to determine transaction to be transient or not.
    * @param transactionId transaction to call
    * @param params        parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapSubmitTransaction(transient: Boolean, transactionId: String, params: String*): String = {
    val result = this.privateSubmitTransaction(transient, transactionId, params: _*)
    this.wrapTransactionResult(transactionId, result)
  }

  /** Wrapper for an evaluation transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transactionId transaction to call
    * @param params        parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapEvaluateTransaction(transactionId: String, params: String*): String = {
    val result = this.privateEvaluateTransaction(transactionId, params: _*)
    this.wrapTransactionResult(transactionId, result)
  }

  protected final def internalGetUnsignedProposal(transactionName: String, params: String*): Array[Byte] = {
    val (transaction, context, request) = TransactionHelper.createTransactionInfo(contract, transactionName, params.toArray, None)

    val proposalBuilder: ProposalBuilder = ProposalBuilder.newBuilder()
      .request(request)
      .context(context)
    val proposal: Proposal = proposalBuilder.build()
    proposal.toByteArray
  }

  final def submitSignedProposal(proposalBytes: Array[Byte], signature: ByteString): String = {
    val proposal: Proposal = Proposal.parseFrom(proposalBytes)

    val (transaction, context, signedProposal) = this.createProposal(proposal, signature)
    val transactionName = TransactionHelper.getTransactionNameFromFcn(transaction.getName)
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

  private def createProposal(proposal: ProposalPackage.Proposal, signature: ByteString): (TransactionImpl, TransactionContext, SignedProposal) = {
    val transactionId: String = TransactionHelper.getTransactionIdFromProposal(proposal)
    val transactionName: String = TransactionHelper.getTransactionNameFromProposal(proposal)
    val params: Seq[String] = TransactionHelper.getTransactionParamsFromProposal(proposal)

    val signedProposalBuilder: SignedProposal.Builder = SignedProposal.newBuilder
      .setProposalBytes(proposal.toByteString)
      .setSignature(signature)
    val signedProposal: SignedProposal = signedProposalBuilder.build

    val (transaction, context, request) = TransactionHelper.createTransactionInfo(contract, transactionName, params.toArray, Some(transactionId))

    (transaction, context, signedProposal)
  }

  private def sendProposalToPeers(context: TransactionContext, signedProposal: ProposalPackage.SignedProposal) = {
    val channel: Channel = contract.getNetwork.getChannel
    val peers: util.Collection[Peer] = ReflectionHelper.safeCallPrivateMethod(channel)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    ReflectionHelper.safeCallPrivateMethod(channel)("sendProposalToPeers")(peers, signedProposal, context).asInstanceOf[util.Collection[ProposalResponse]]
  }

  @throws[HyperledgerExceptionTrait]
  private def privateSubmitTransaction(transient: Boolean, transactionName: String, params: String*): Array[Byte] = {
    testParamsNull(transactionName, params: _*)
    try {
      if (transient) {
        var transMap: Map[String, Array[Byte]] = Map()
        var i = 0
        params.foreach(param => {
          transMap += i.toString -> param.toCharArray.map(_.toByte)
          i = i + 1
        })

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
    testParamsNull(transactionName, params: _*)
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
  private def testParamsNull(transactionName: String, params: String*): Unit = {
    params.foreach(param => if (param == null) throw TransactionException.CreateUnknownException(transactionName, "A parameter was null."))
  }
}
