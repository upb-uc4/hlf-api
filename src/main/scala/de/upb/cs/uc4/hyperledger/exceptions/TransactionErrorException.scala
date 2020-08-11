package de.upb.cs.uc4.hyperledger.exceptions

/**
 *  Legacy Error from old courses
 * @param transactionId
 * @param errorCode
 * @param errorDetail
 */
case class TransactionErrorException(transactionId: String, errorCode: Integer, errorDetail: String) extends Exception {
  override def toString(): String =
    "The provided transaction: \"" + transactionId + "\" failed with an error: " + errorCode + " : " + errorDetail
}
