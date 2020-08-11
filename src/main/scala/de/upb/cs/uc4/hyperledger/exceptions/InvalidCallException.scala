package de.upb.cs.uc4.hyperledger.exceptions

case class InvalidCallException(transactionId: String, jsonError: String) extends TransactionExceptionTrait {
  override def toString: String =
    s"The transaction: '${this.transactionId}' could not be invoked.\nDetails: ${this.jsonError}"
}

object InvalidCallException {

  final val jSonUnknown = "{\n" +
    "  \"type\": \"hl: unknown transactionId\",\n" +
    "  \"title\": \"The transaction is not defined.\"\n" +
    "}"

  final def jSonWrongInvoke(transactionId: String, expected: Integer, actual: Integer) = "{\n" +
    "  \"type\": \"hl: invalid transaction call\",\n" +
    "  \"title\": \"The transaction was invoked with the wrong amount of parameters. Expected: "+ expected + " Actual: " + actual + "\",\n" +
    "  \"transactionId\": \"" + transactionId + "\"\n" +
    "}"

  def CreateInvalidIDException(transactionId: String): InvalidCallException =
    new InvalidCallException(transactionId, InvalidCallException.jSonUnknown)

  def CreateInvalidParameterCountException(transactionId: String, expected: Integer, actual: Integer): InvalidCallException =
    new InvalidCallException(transactionId, InvalidCallException.jSonWrongInvoke(transactionId, expected, actual)
      )
}
