package de.upb.cs.uc4.hyperledger.exceptions.traits.internal

/** Override general Exception Methods to provide a better working experience.
  *
  */
protected[exceptions] trait UC4ExceptionTrait extends Exception {
  /** Override getMessage for better error logging.
    *
    * @return String describing the error.
    */
  override def getMessage: String = toString
}
