package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util
import java.util.concurrent.TimeoutException

import com.google.gson.Gson
import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.cases.ConnectionApproval
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, ReflectionHelper, TransactionHelper }
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.gateway.GatewayRuntimeException
import org.hyperledger.fabric.protos.common.Common.Payload
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ Proposal, SignedProposal }
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.TransactionContext

import scala.jdk.CollectionConverters
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
  def approvalConnection: Option[ConnectionApprovalsTrait] = Some(ConnectionApproval(username, channel, chaincode, walletPath, networkDescriptionPath))

  /** Gets the version returned by the designated contract.
    * By default all contracts return the version of the chaincode.
    *
    * @return String containing versionInfo
    */
  def getChaincodeVersion: String = wrapEvaluateTransaction("getVersion")

  private def approveTransaction(transactionName: String, params: String*): String = {
    var approvalResult: String = ""
    // setup approvalConnection and
    // submit my approval to approvalContract
    val approvalConnectionObject = approvalConnection
    if (approvalConnectionObject.isDefined) {
      approvalResult = approvalConnectionObject.get.approveTransaction(contractName, transactionName, params: _*)
      approvalConnectionObject.get.close()
    }
    approvalResult
  }

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
    approveTransaction(transactionName, params: _*)

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
    approveTransaction(transactionName, params: _*)

    val result = this.privateEvaluateTransaction(transactionName, params: _*)
    this.wrapTransactionResult(transactionName, result)
  }

  // TODO read affiliation from certificate
  protected final def internalGetUnsignedProposal(certificate: String, affiliation: String, transactionName: String, params: String*): (String, Array[Byte]) = {
    // approve transaction as ADMIN managing the current connection
    val approvalResult: String = approveTransaction(transactionName, params: _*)

    // prepare the approvalTransaction for the user.
    val fcnName: String = "UC4.Approval:approveTransaction"
    val args: Seq[String] = Seq(this.contractName).appended(transactionName).appended(new Gson().toJson(params.toArray))
    val proposal = TransactionHelper.getUnsignedProposalNew(certificate, affiliation, chaincode, channel, fcnName, networkDescriptionPath, args: _*)
    val proposalBytes = proposal.toByteArray

    // return both (approvalResult and proposal)
    (approvalResult, proposalBytes)
  }

  def getUnsignedTransaction(proposalBytes: Array[Byte], signatureBytes: Array[Byte]): Array[Byte] = {
    // create signedProposal Object and get Info Objects
    val signature = ByteString.copyFrom(signatureBytes)
    val proposal = Proposal.parseFrom(proposalBytes)
    val (transaction: TransactionImpl, signedProposal: SignedProposal) =
      TransactionHelper.createSignedProposal(approvalConnection.get, proposal, signature)

    // submit approval proposal
    val channelObj: Channel = this.gateway.getNetwork(channel).getChannel
    val peers: util.Collection[Peer] = ReflectionHelper.safeCallPrivateMethod(channelObj)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    val transactionProposal = Proposal.parseFrom(proposalBytes)
    val transactionName = TransactionHelper.getTransactionNameFromProposal(transactionProposal)
    val transactionParams = TransactionHelper.getTransactionParamsFromProposal(transactionProposal)
    val transactionId = TransactionHelper.getTransactionIdFromProposal(transactionProposal)
    val (_, ctx: TransactionContext, _) = TransactionHelper.createTransactionInfo(this.approvalConnection.get.contract, transactionName, transactionParams, Some(transactionId))
    val proposalResponses = ReflectionHelper.safeCallPrivateMethod(channelObj)("sendProposalToPeers")(peers, signedProposal, ctx).asInstanceOf[util.Collection[ProposalResponse]]

    val validResponses = ReflectionHelper.safeCallPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]
    val transactionPayload = TransactionHelper.getTransaction(validResponses, channelObj)
    transactionPayload.toByteArray
  }

  def submitSignedTransaction(transactionBytes: Array[Byte], signature: Array[Byte]): (String, String) = {
    val transactionPayload: Payload = Payload.parseFrom(transactionBytes)
    val transactionId: String = TransactionHelper.getTransactionIdFromHeader(transactionPayload.getHeader)
    val (transactionName, params) = TransactionHelper.getParametersFromTransactionPayload(transactionPayload)
    val (_, ctx, _) = TransactionHelper.createTransactionInfo(this.approvalConnection.get.contract, transactionName, params, Some(transactionId))
    val response: Array[Byte] = TransactionHelper.sendTransaction(this, channel, ctx, this.gateway.getNetwork(channel).getChannel, ByteString.copyFrom(transactionBytes), signature, transactionId)
    val approvalResult = wrapTransactionResult(transactionName, response)

    // execute real Transaction
    val realResult = internalSubmitRealTransactionFromApprovalProposal(params)

    // return both results
    (approvalResult, realResult)
  }

  def internalSubmitRealTransactionFromApprovalProposal(params: Seq[String]): String = {
    val realContractName: String = params.head
    val realTransactionName: String = params.tail.head
    val realTransactionParamsString: String = params.tail.tail.head
    val realTransactionParamsArrayList: util.ArrayList[String] = new Gson().fromJson(realTransactionParamsString, classOf[util.ArrayList[String]])
    val realTransactionParams: Seq[String] = CollectionConverters.IterableHasAsScala(realTransactionParamsArrayList).asScala.toSeq

    // Logging
    Logger.warn("contractName" + realContractName)
    Logger.warn("transactionName" + realTransactionName)
    Logger.warn("params" + realTransactionParams.mkString(", "))

    // check contract match
    if (realContractName != this.contractName) throw TransactionException.CreateUnknownException("approveTransaction", s"Approval was sent to wrong connection:: $contractName != $realContractName")

    // submit and evaluate response from my "regular" contract
    // TODO: pass transient bool
    val result = this.privateSubmitTransaction(false, realTransactionName, realTransactionParams: _*)
    this.wrapTransactionResult(realTransactionName, result)
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
    Logger.info("TRANSACTION RESULT:: " + resultString)
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
    if (params.contains(null)) throw TransactionException.CreateUnknownException(transactionName, "A parameter was null.")
  }
}
