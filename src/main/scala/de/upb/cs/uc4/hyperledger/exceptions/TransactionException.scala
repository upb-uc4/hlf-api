package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait

/**
 *  Exception Trait to wrap any Exception thrown from the Hyperledger Framework
 */
case class TransactionException(transactionId: String, payload: String) extends TransactionExceptionTrait{
  override def toString: String =
    s"The provided transaction: '${transactionId}' failed with an error: ${payload}"
}

/**
 * Used to create TransactionExceptions from malformed errors.
 */
object TransactionException {
  final def jSonUnknown(id: String, detail: String) = "{\n" +
    "  \"type\": \"" + id + "\",\n" +
    "  \"title\": \"" + detail + "\"\n" +
    "}"

  def CreateUnknownException(id: String, detail: String): TransactionException =
    new TransactionException(id, TransactionException.jSonUnknown(id, detail)
    )
}