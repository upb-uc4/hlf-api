package de.upb.cs.uc4.hyperledger.connections.traits

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.exceptions.{ HyperledgerInnerException, HyperledgerUnhandledException, TransactionException }
import de.upb.cs.uc4.hyperledger.utilities.GatewayManager
import org.hyperledger.fabric.gateway.{ Contract, ContractException, Gateway, GatewayRuntimeException }

import scala.jdk.CollectionConverters.MapHasAsJava

trait ConnectionTrait extends AutoCloseable {
  val contractName: String
  val contract: Contract
  val gateway: Gateway

  @throws[HyperledgerExceptionTrait]
  protected final def internalSubmitTransaction(transient: Boolean, transactionId: String, params: String*): Array[Byte] = {
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
      case ex: ContractException => throw HyperledgerInnerException(transactionId, ex)
      case ex: TimeoutException => throw HyperledgerInnerException(transactionId, ex)
      case ex: java.lang.InterruptedException => throw HyperledgerInnerException(transactionId, ex)
      case ex: GatewayRuntimeException => throw HyperledgerInnerException(transactionId, ex)
      case ex: Exception => throw HyperledgerUnhandledException(transactionId, ex)
    }
  }

  @throws[HyperledgerExceptionTrait]
  protected final def internalEvaluateTransaction(transactionId: String, params: String*): Array[Byte] = {
    try {
      contract.evaluateTransaction(transactionId, params: _*)
    }
    catch {
      case ex: ContractException => throw HyperledgerInnerException(transactionId, ex)
      case ex: Exception         => throw HyperledgerUnhandledException(transactionId, ex)
    }
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
  @throws[TransactionException]
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

  final override def close(): Unit = GatewayManager.disposeGateway(this.gateway)
}
