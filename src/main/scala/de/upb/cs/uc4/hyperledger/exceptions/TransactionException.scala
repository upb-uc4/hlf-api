package de.upb.cs.uc4.hyperledger.exceptions

import de.upb.cs.uc4.hyperledger.exceptions.traits.TransactionExceptionTrait

/** Exception Trait to wrap any Exception thrown from the Hyperledger Framework */
protected case class TransactionException(
    transactionName: String,
    payload: String
) extends TransactionExceptionTrait {
  override def toString: String =
    s"The provided transaction: '$transactionName' failed with an error: $payload"
}

/** Used to create TransactionExceptions from malformed errors. */
protected[hyperledger] object TransactionException {

  final private def jSonUnknown(id: String, detail: String): String = "{\n" +
    "  \"type\": \"" + id + "\",\n" +
    "  \"title\": \"" + detail + "\"\n" +
    "}"

  def CreateUnknownException(id: String, detail: String): TransactionException =
    new TransactionException(id, TransactionException.jSonUnknown(id, detail))

  def Create(id: String, payload: String): TransactionException =
    new TransactionException(id, payload)
}