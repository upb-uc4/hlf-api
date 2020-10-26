package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.util
import java.util.concurrent.TimeoutException

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.helper.ReflectionHelper
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.gateway.{ ContractException, GatewayRuntimeException, Transaction }
import org.hyperledger.fabric.protos.peer.ProposalPackage
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionContext }

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  // regular contract info
  val contractName: String
  val contract: ContractImpl
  val gateway: GatewayImpl

  // approval contract
  val approvalConnection: Option[ConnectionApprovalsTrait]

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

  protected final def internalGetUnsignedProposal(transactionName: String, params: String*): (Array[Byte], String) = {
    val (proposal: Proposal, id: String) = this.createUnsignedTransaction(transactionName, params: _*)
    (proposal.toByteArray, id)
  }

  final def submitSignedProposal(proposalBytes: Array[Byte], signature: ByteString, transactionName: String, transactionId: String, params: String*): Array[Byte] = {
    val proposal: Proposal = Proposal.parseFrom(proposalBytes)
    val (transaction, context, signedProposal) = createProposal(proposal, signature, transactionName, transactionId, params: _*)
    val proposalResponses = sendProposalToPeers(context, signedProposal)
    val validResponses = ReflectionHelper.callPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]
    commitTransaction(transaction, proposalResponses, validResponses)
  }

  private final def createUnsignedTransaction(transactionName: String, params: String*): (Proposal, String) = {
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.callPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = ReflectionHelper.callPrivateMethod(contract.getNetwork.getChannel)("getTransactionContext")(request).asInstanceOf[TransactionContext]
    val proposalBuilder: ProposalBuilder = ProposalBuilder.newBuilder()
    proposalBuilder.context(context)
    proposalBuilder.request(request)
    (proposalBuilder.build(), context.getTxID)
  }

  private def createProposal(proposal: ProposalPackage.Proposal, signature: ByteString, transactionName: String, transactionId: String, params: String*) = {
    val signedProposalBuilder: ProposalPackage.SignedProposal.Builder = ProposalPackage.SignedProposal.newBuilder
    val signedProposal: ProposalPackage.SignedProposal = signedProposalBuilder.setProposalBytes(proposal.toByteString).setSignature(signature).build
    val transaction: TransactionImpl = contract.createTransaction(transactionName).asInstanceOf[TransactionImpl]
    val request: TransactionProposalRequest = ReflectionHelper.callPrivateMethod(transaction)("newProposalRequest")(params.toArray).asInstanceOf[TransactionProposalRequest]
    val context: TransactionContext = ReflectionHelper.callPrivateMethod(contract.getNetwork.getChannel)("getTransactionContext")(request).asInstanceOf[TransactionContext]
    ReflectionHelper.setPrivateField(context)("txID")(transactionId)
    context.verify(request.doVerify())
    context.setProposalWaitTime(request.getProposalWaitTime)
    ReflectionHelper.setPrivateField(transaction)("transactionContext")(context)
    (transaction, context, signedProposal)
  }

  private def sendProposalToPeers(context: TransactionContext, signedProposal: ProposalPackage.SignedProposal) = {
    val channel: Channel = contract.getNetwork.getChannel
    val peers: util.Collection[Peer] = ReflectionHelper.callPrivateMethod(channel)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    ReflectionHelper.callPrivateMethod(channel)("sendProposalToPeers")(peers, signedProposal, context).asInstanceOf[util.Collection[ProposalResponse]]
  }

  private def commitTransaction(transaction: Transaction, proposalResponses: util.Collection[ProposalResponse], validResponses: util.Collection[ProposalResponse]) = {
    try ReflectionHelper.callPrivateMethod(transaction)("commitTransaction")(validResponses).asInstanceOf[Array[Byte]]
    catch {
      case e: ContractException =>
        e.setProposalResponses(proposalResponses)
        throw e
    }
    //} catch {
    //  case e@(_: InvalidArgumentException | _: ProposalException | _: ServiceDiscoveryException) =>
    //   throw new GatewayRuntimeException(e)
    //}
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
