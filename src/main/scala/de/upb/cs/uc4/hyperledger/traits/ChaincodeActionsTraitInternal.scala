package de.upb.cs.uc4.hyperledger.traits

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException

import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.exceptions.{HyperledgerInnerException, InvalidCallException, TransactionException, UnhandledException}
import org.hyperledger.fabric.gateway.{Contract, ContractException, GatewayRuntimeException}

/**
 * Trait to provide basic functionality for all chaincode transactions.
 */
protected trait ChaincodeActionsTraitInternal extends AutoCloseable {

  @throws[HyperledgerExceptionTrait]
  protected final def internalSubmitTransaction(chaincode: Contract, transactionId: String, params: String*): Array[Byte] = {
    try {
      chaincode.submitTransaction(transactionId, params: _*)
    } catch {
      case ex: ContractException => throw HyperledgerInnerException(transactionId, ex)
      case ex: TimeoutException => throw HyperledgerInnerException(transactionId, ex)
      case ex: java.lang.InterruptedException => throw HyperledgerInnerException(transactionId, ex)
      case ex: GatewayRuntimeException => throw HyperledgerInnerException(transactionId, ex)
      case ex: Exception => throw UnhandledException(transactionId, ex)
    }
  }

  @throws[HyperledgerExceptionTrait]
  protected final def internalEvaluateTransaction(chaincode: Contract, transactionId: String, params: String*): Array[Byte] = {
    try {
      chaincode.evaluateTransaction(transactionId, params: _*)
    } catch {
      case ex: ContractException => throw HyperledgerInnerException(transactionId, ex)
      case ex: Exception => throw UnhandledException(transactionId, ex)
    }
  }

  /**
   * Since the chain returns bytes, we need to convert them to a readable Result.
   *
   * @param result Bytes containing a result from a chaincode transaction.
   * @return Result as a String.
   */
  protected def convertTransactionResult(result: Array[Byte]): String = {
    new String(result, StandardCharsets.UTF_8)
  }

  /**
   * Used to validate the parameter count.
   * Will throw an exception if parametercount not as expected.
   *
   * @param transactionId
   * @param expected
   * @param params
   */
  @throws[InvalidCallException]
  protected def validateParameterCount(transactionId: String, expected: Integer, params: Array[String]): Unit = {
    if (params.size != expected) throw InvalidCallException.CreateInvalidParameterCountException(transactionId, expected, params.size)
  }

  /**
   * Wraps the chaincode query result bytes.
   * Translates the byte-array to a string and throws an error if said string is not empty
   *
   * @param result input byte-array to translate
   * @return result as a string
   */
  @throws[TransactionException]
  def wrapTransactionResult(transactionId: String, result: Array[Byte]): String = {
    val resultString = convertTransactionResult(result)
    if (containsError(resultString)) throw TransactionException(transactionId, resultString)
    else resultString
  }

  /**
   * Evaluates whether a transactionResult contains a "detailedError" or a "genericError"
   *
   * @param result result of a chaincode transaction
   * @return true if the result contains error information conforming to API-standards
   */
  private def containsError(result: String): Boolean = {
    result.contains("{\"type\":") && result.contains("\"title\":")
  }
}
