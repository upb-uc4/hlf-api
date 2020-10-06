package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerException, NetworkException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.WalletManager
import org.hyperledger.fabric.gateway.impl.{ ContractImpl, GatewayImpl }
import org.hyperledger.fabric.gateway.{ Contract, Gateway, GatewayRuntimeException, Identity, Transaction, X509Identity }
import org.hyperledger.fabric.protos.peer.ProposalPackage
import org.hyperledger.fabric.sdk.HFClient
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo
import org.hyperledger.fabric.sdk.transaction.{ ProposalBuilder, TransactionContext }

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  val contractName: String
  val contract: ContractImpl
  val gateway: GatewayImpl

  @throws[HyperledgerExceptionTrait]
  protected final def internalSubmitTransaction(transient: Boolean, transactionId: String, params: String*): Array[Byte] = {
    testParamsNull(transactionId, params: _*)
    try {
      if (transient) {
        var transMap: Map[String, Array[Byte]] = Map()
        var i = 0
        params.foreach(param => {
          transMap += i.toString -> param.toCharArray.map(_.toByte)
          i = i + 1
        })

        contract.createTransaction(transactionId).setTransient(transMap.asJava).submit()
      }
      else {
        contract.submitTransaction(transactionId, params: _*)
      }
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionId, ex)
    }
  }

  @throws[HyperledgerExceptionTrait]
  protected final def internalEvaluateTransaction(transactionId: String, params: String*): Array[Byte] = {
    testParamsNull(transactionId, params: _*)
    try {
      contract.evaluateTransaction(transactionId, params: _*)
    }
    catch {
      case ex: GatewayRuntimeException => throw NetworkException(innerException = ex)
      case ex: TimeoutException        => throw NetworkException(innerException = ex)
      case ex: Exception               => throw HyperledgerException(transactionId, ex)
    }
  }

  final def createUnsignedTransaction(transactionId: String, params: String*): ProposalPackage.Proposal = {
    val client = gateway.getClient()
    val request = client.newTransactionProposalRequest()
    request.setChaincodeName(contractName)
    request.setFcn(transactionId)
    request.setArgs(params: _*)
    val context: TransactionContext = contract.getNetwork.getChannel.newTransactionContext()

    val proposalBuilder: ProposalBuilder = ProposalBuilder.newBuilder
    proposalBuilder.context(context)
    proposalBuilder.request(request)
    proposalBuilder.build()
  }

  /** Since the chain returns bytes, we need to convert them to a readable Result.
    *
    * @param result Bytes containing a result from a chaincode transaction.
    * @return Result as a String.
    */
  protected final def convertTransactionResult(result: Array[Byte]): String = {
    new String(result, StandardCharsets.UTF_8)
  }

  /** Wraps the chaincode query result bytes.
    * Translates the byte-array to a string and throws an error if said string is not empty
    *
    * @param result input byte-array to translate
    * @return result as a string
    */
  @throws[TransactionExceptionTrait]
  protected final def wrapTransactionResult(transactionId: String, result: Array[Byte]): String = {
    val resultString = convertTransactionResult(result)
    if (containsError(resultString)) throw TransactionException(transactionId, resultString)
    else resultString
  }

  /** Evaluates whether a transactionResult contains a "detailedError" or a "genericError"
    *
    * @param result result of a chaincode transaction
    * @return true if the result contains error information conforming to API-standards
    */
  private def containsError(result: String): Boolean = {
    result.contains("{\"type\":") && result.contains("\"title\":")
  }

  final override def close(): Unit = this.gateway.close()

  /** Checks if the transaction params are null.
    *
    * @param transactionId transactionId causing the error.
    * @param params parameters to check
    * @throws TransactionException if a parameter is null.
    */
  @throws[TransactionExceptionTrait]
  private def testParamsNull(transactionId: String, params: String*): Unit = {
    params.foreach(param => if (param == null) throw TransactionException.CreateUnknownException(transactionId, "A parameter was null."))
  }
}
