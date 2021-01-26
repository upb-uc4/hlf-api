package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util
import java.util.concurrent.TimeoutException

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.cases.{ ConnectionAdmission, ConnectionCertificate, ConnectionExaminationRegulation, ConnectionGroup, ConnectionMatriculation, ConnectionOperation }
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, NetworkExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager
import de.upb.cs.uc4.hyperledger.utilities.helper.{ CertificateHelper, Logger, ReflectionHelper, StringHelper, TransactionHelper }
import org.hyperledger.fabric.gateway.GatewayRuntimeException
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl, TransactionImpl }
import org.hyperledger.fabric.protos.common.Common.Payload
import org.hyperledger.fabric.protos.peer.ProposalPackage.{ Proposal, SignedProposal }
import org.hyperledger.fabric.sdk._
import org.hyperledger.fabric.sdk.transaction.TransactionContext

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  // setting up connection
  lazy val (contract: ContractImpl, gateway: GatewayImpl) = ConnectionManager.initializeConnection(username, channel, chaincode, contractName, walletPath, networkDescriptionPath)
  // setup approvalConnection
  lazy val operationsConnection: Option[ConnectionOperationTrait] = Some(ConnectionOperation(username, channel, chaincode, walletPath, networkDescriptionPath))
  val AFFILIATION: String = "org1MSP"
  // regular info used to set up any connection
  val username: String
  val channel: String
  val chaincode: String
  val walletPath: Path
  val networkDescriptionPath: Path
  // contract info for specific connections
  val contractName: String

  var timeoutMilliseconds: Int = 6000
  var timeoutAttempts: Int = 10
  final def timeoutMilliseconds(newVal: Int): ConnectionTrait = {
    this.timeoutMilliseconds = newVal
    this
  }
  final def timeoutAttempts(newVal: Int): ConnectionTrait = {
    this.timeoutAttempts = newVal
    this
  }
  final override def close(): Unit = {
    if (this.gateway != null) {
      this.gateway.close()
    }
  }

  /** Gets the version returned by the designated contract.
    * By default all contracts return the version of the chaincode.
    *
    * @return String containing versionInfo
    */
  def getChaincodeVersion: String = wrapEvaluateTransaction("getVersion")

  /** Wrapper for a submission transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transient       boolean flag to determine transaction to be transient or not.
    * @param transactionName transaction to call
    * @param params          parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapSubmitTransaction(transient: Boolean, transactionName: String, params: String*): String = {
    // submit and evaluate response from my "regular" contract
    val resultBytes = ReflectionHelper.retryAction(() => this.privateSubmitTransaction(transient, transactionName, params: _*), transactionName, timeoutMilliseconds, timeoutAttempts)
    this.wrapTransactionResult(transactionName, resultBytes)
  }

  @throws[HyperledgerExceptionTrait]
  private def privateSubmitTransaction(transient: Boolean, transactionName: String, params: String*): Array[Byte] = {
    Logger.info(s"Submit Transaction: '$transactionName' with parameters: $params")

    testAnyParamsNull(transactionName, params: _*)
    try {
      if (transient) {
        var i = 0
        var transMap: Map[String, Array[Byte]] = Map()
        params.foreach(param => {
          transMap += i.toString -> param.toCharArray.map(_.toByte)
          i = i + 1
        })

        // TODO: once transient is re-enabled, test this
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

  /** Wrapper for an evaluation transaction
    * Translates the result byte-array to a string and throws an error if said string contains an error.
    *
    * @param transactionName transaction to call
    * @param params          parameters to feed into transaction
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  @throws[HyperledgerExceptionTrait]
  protected final def wrapEvaluateTransaction(transactionName: String, params: String*): String = {
    val result = this.privateEvaluateTransaction(transactionName, params: _*)
    this.wrapTransactionResult(transactionName, result)
  }

  @throws[HyperledgerExceptionTrait]
  @throws[NetworkExceptionTrait]
  private def privateEvaluateTransaction(transactionName: String, params: String*): Array[Byte] = {
    Logger.info(s"Evaluate Transaction: '$transactionName' with parameters: $params")
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
    if (chaincodeResultContainsError(resultString)) throw TransactionException(transactionName, resultString)
    else resultString
  }

  /** Evaluates whether a transactionResult contains a "detailedError" or a "genericError"
    *
    * @param result result of a chaincode transaction
    * @return true if the result contains error information conforming to API-standards
    */
  private def chaincodeResultContainsError(result: String): Boolean = result.contains("{\"type\":") && result.contains("\"title\":")

  // TODO read affiliation from certificate
  @throws[HyperledgerExceptionTrait]
  @throws[NetworkExceptionTrait]
  @throws[TransactionExceptionTrait]
  protected final def internalApproveAsCurrentAndGetProposalProposeTransaction(certificate: String, affiliation: String, transactionName: String, params: String*): (String, Array[Byte]) = {
    // approve transaction as user of current connection (mostly LAGOM_ADMIN)
    // if the transaction is invalid, the "approveAsCurrent" method will throw an exception
    val initiator = CertificateHelper.getNameFromCertificate(certificate)
    val adminApprovalResult: String = approveAsCurrent(initiator, transactionName, params)

    // prepare the approveOperationProposal for the user.
    val operationId = StringHelper.getOperationIdFromOperation(adminApprovalResult)
    val proposalBytes: Array[Byte] = getProposalForApprove(certificate, affiliation, operationId)

    // return both (adminApprovalResult and proposal)
    (adminApprovalResult, proposalBytes)
  }

  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  private def approveAsCurrent(initiator: String, transactionName: String, params: Seq[String]): String = {
    var approvalResult: String = ""
    // submit my approval to operationsContract
    if (operationsConnection.isDefined) {
      approvalResult = operationsConnection.get.initiateOperation(initiator, contractName, transactionName, params: _*)
    }
    approvalResult
  }

  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  private def getProposalForApprove(certificate: String, affiliation: String, operationId: String): Array[Byte] = {
    var proposal: Array[Byte] = null

    // submit my approval to operationsContract
    if (operationsConnection.isDefined) {
      proposal = operationsConnection.get.getProposalApproveOperation(certificate, affiliation, operationId)
    }
    proposal
  }

  /** Trades a proposal plus signature for a new transaction that can be signed.
    *
    * @param proposalBytes  The original proposal for which a transaction shall be created
    * @param signatureBytes The signature of the original proposal
    * @return The newly created transaction.
    */
  def getUnsignedTransaction(proposalBytes: Array[Byte], signatureBytes: Array[Byte]): Array[Byte] = {
    // create signedProposal Object and get Info Objects
    val signature = ByteString.copyFrom(signatureBytes)
    val proposal = Proposal.parseFrom(proposalBytes)
    val (transaction: TransactionImpl, signedProposal: SignedProposal) =
      TransactionHelper.createSignedProposal(this.asInstanceOf[ConnectionOperationTrait], proposal, signature)

    // submit approval proposal
    val channelObj: Channel = this.gateway.getNetwork(channel).getChannel
    val peers: util.Collection[Peer] = ReflectionHelper.safeCallPrivateMethod(channelObj)("getEndorsingPeers")().asInstanceOf[util.Collection[Peer]]
    val transactionProposal = Proposal.parseFrom(proposalBytes)
    val transactionName = TransactionHelper.getTransactionNameFromProposal(transactionProposal)
    val transactionParams = TransactionHelper.getTransactionParamsFromProposal(transactionProposal)
    val transactionId = TransactionHelper.getTransactionIdFromProposal(transactionProposal)
    val (_, ctx: TransactionContext, _) = TransactionHelper.createTransactionInfo(this.contract, transactionName, transactionParams, Some(transactionId))
    // send to peers
    val proposalResponses = ReflectionHelper.safeCallPrivateMethod(channelObj)("sendProposalToPeers")(peers, signedProposal, ctx).asInstanceOf[util.Collection[ProposalResponse]]
    val validResponses = ReflectionHelper.safeCallPrivateMethod(transaction)("validatePeerResponses")(proposalResponses).asInstanceOf[util.Collection[ProposalResponse]]

    // create final transaction to submit
    val transactionPayload = ReflectionHelper.retryAction(() => TransactionHelper.getTransaction(validResponses, channelObj), "getUnsignedTransaction", timeoutMilliseconds, timeoutAttempts);

    transactionPayload.toByteArray
  }

  /** Submits a given approval transaction and it's corresponding "real" transaction
    *
    * @param transactionBytes approvalTransaction bytes submitted
    * @param signature        the signature authenticating the user
    * @return Tuple containing (approvalResult, realTransactionResult)
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def submitSignedTransaction(transactionBytes: Array[Byte], signature: Array[Byte]): String = {
    val transactionPayload: Payload = Payload.parseFrom(transactionBytes)
    val transactionId: String = TransactionHelper.getTransactionIdFromHeader(transactionPayload.getHeader)
    val (transactionName, params) = TransactionHelper.getParametersFromTransactionPayload(transactionPayload)
    val (_, ctx, _) = TransactionHelper.createTransactionInfo(this.contract, transactionName, params, Some(transactionId))
    val response: Array[Byte] = TransactionHelper.sendTransaction(this, channel, ctx, this.gateway.getNetwork(channel).getChannel, ByteString.copyFrom(transactionBytes), signature, transactionId)
    wrapTransactionResult(transactionName, response)
  }

  /** Submits a given approval transaction and it's corresponding "real" transaction
    *
    * @param jsonOperationData operationData json containing transactionInfo
    * @return Tuple containing (approvalResult, realTransactionResult)
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def executeTransaction(jsonOperationData: String, timeoutMilliseconds: Int = 30000, timeoutAttempts: Int = Int.MaxValue): String = {
    // prepare info
    val transactionInfo = StringHelper.getTransactionInfoFromOperation(jsonOperationData)
    val (contractName, transactionName, transactionParams) = StringHelper.getInfoFromTransactionInfo(transactionInfo)
    val transactionTransient = false // TODO: read transient bool from params

    // build connection
    val connection: ConnectionTrait = buildConnectionForContract(contractName)

    // attempt transmission
    connection.wrapSubmitTransaction(transactionTransient, transactionName, transactionParams: _*)
  }

  private def buildConnectionForContract(contractName: String): ConnectionTrait = {
    contractName match {
      case "UC4.Admission" => ConnectionAdmission(this.username, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
      case "UC4.Certificate" => ConnectionCertificate(this.username, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
      case "UC4.ExaminationRegulation" => ConnectionExaminationRegulation(this.username, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
      case "UC4.Group" => ConnectionGroup(this.username, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
      case "UC4.MatriculationData" => ConnectionMatriculation(this.username, this.channel, this.chaincode, this.walletPath, this.networkDescriptionPath)
      // catch the default with a variable so you can print it
      case _ => throw new Exception(s"Cannot find suitable connection for contract: $contractName")
    }
  }
}
