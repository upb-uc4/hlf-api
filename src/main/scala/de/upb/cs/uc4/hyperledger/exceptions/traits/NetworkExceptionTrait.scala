package de.upb.cs.uc4.hyperledger.exceptions.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.internal.UC4ExceptionTrait

/** Exception Trait to wrap any Exception thrown from our chaincode.
  *  Any validation, misuse of transaction, or similar errors.
  */
trait NetworkExceptionTrait extends UC4ExceptionTrait {

  /**  channel name you used when you tried to access the network */
  val channel: String

  /**  chaincode name you used when you tried to access the network */
  val chaincode: String

  /**  networkDescription you used when you tried to access the network */
  val networkDescription: String

  /**  identity name you used when you tried to access the network */
  val identity: String

  /**  organisationId you used when you tried to access the network */
  val organisationId: String

  /**  inner Exception */
  val innerException: Exception
}
