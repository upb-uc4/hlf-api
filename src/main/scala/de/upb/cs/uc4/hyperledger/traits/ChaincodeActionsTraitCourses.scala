package de.upb.cs.uc4.hyperledger.traits

import de.upb.cs.uc4.hyperledger.exceptions.TransactionErrorException
import org.hyperledger.fabric.gateway.Contract

/**
 * Trait to provide explicit access to chaincode transactions regarding courses
 */
trait ChaincodeActionsTraitCourses extends ChaincodeActionsTraitInternal {

  def contract_course: Contract

  /**
   * Submits any transaction specified by transactionId.
   *
   * @param transactionId transactionId to submit
   * @param params        parameters to pass to the transaction
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionErrorException]
  private def internalSubmitTransaction(transactionId: String, params: String*): Array[Byte] =
    this.internalSubmitTransaction(contract_course, transactionId, params: _*)

  /**
   * Evaluates the transaction specified by transactionId.
   *
   * @param transactionId transactionId to evaluate
   * @param params        parameters to pass to the transaction
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionErrorException]
  private def internalEvaluateTransaction(transactionId: String, params: String*): Array[Byte] =
    this.internalEvaluateTransaction(contract_course, transactionId, params: _*)

  /**
   * Executes the "addCourse" query.
   *
   * @param jSonCourse Information about the course to add.
   * @throws Exception if chaincode throws an exception.
   * @return Success_state
   */
  @throws[TransactionErrorException]
  final def addCourse(jSonCourse: String): String =
    this.customWrapTransactionResult("addCourse", this.internalSubmitTransaction("addCourse", jSonCourse))

  /**
   * Submits the "deleteCourseById" query.
   *
   * @param courseId courseId to delete course
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionErrorException]
  final def deleteCourseById(courseId: String): String =
    this.customWrapTransactionResult("deleteCourseById", this.internalSubmitTransaction("deleteCourseById", courseId))

  /**
   * Submits the "updateCourseById" query.
   *
   * @param courseId   courseId to update course
   * @param jSonCourse courseInfo to update to
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[TransactionErrorException]
  final def updateCourseById(courseId: String, jSonCourse: String): String =
    this.customWrapTransactionResult("updateCourseById", this.internalSubmitTransaction("updateCourseById", courseId, jSonCourse))

  /**
   * Executes the "getCourses" query.
   *
   * @throws Exception if chaincode throws an exception.
   * @return List of courses represented by their json value.
   */
  @throws[TransactionErrorException]
  final def getAllCourses: String = {
    val result = this.customWrapTransactionResult("getAllCourses", this.internalEvaluateTransaction("getAllCourses"))

    // check specific error
    if (!result.startsWith("[") || !result.endsWith("]")) throw TransactionErrorException("getAllCourses", 0, "Returned invalid structure: " + result)
    else result
  }

  /**
   * Executes the "getCourseById" query.
   *
   * @param courseId courseId to get course information
   * @throws Exception if chaincode throws an exception.
   * @return JSon Course Object
   */
  @throws[TransactionErrorException]
  final def getCourseById(courseId: String): String = {
    val result = this.customWrapTransactionResult("getCourseById", this.internalEvaluateTransaction("getCourseById", courseId))

    // check specific error
    if (result == "null") throw TransactionErrorException("getCourseById", 0, "Returned null.")
    else result
  }

  /**
   * Wraps the chaincode query result bytes.
   * Translates the byte-array to a string and throws an error if said string is not empty
   * Overridden due to specific errors occurring in courses
   *
   * @param result input byte-array to translate
   * @return result as a string
   */
  @throws[TransactionErrorException]
  private def customWrapTransactionResult(transactionId: String, result: Array[Byte]): String = {
    val resultString = convertTransactionResult(result)
    if (containsError(resultString)) throw extractErrorFromResult(transactionId, resultString)
    else resultString
  }

  private def extractErrorFromResult(transactionId: String, result: String): TransactionErrorException = {
    // retrieve error code
    var id = result.substring(result.indexOf("\"name\":\"") + 8)
    id = id.substring(0, id.indexOf("\""))

    // retrieve detail
    var detail = result.substring(result.indexOf("\"detail\":\"") + 10)
    detail = detail.substring(0, detail.indexOf("\""))

    // create Exception
    TransactionErrorException(transactionId, Integer.parseInt(id), detail)
  }

  /**
   * Evaluates whether a transaction was valid or invalid
   *
   * @param result result of a chaincode transaction
   * @return true if the result contains error information
   */
  private def containsError(result: String): Boolean = {
    result.contains("{\"name\":") && result.contains("\"detail\":")
  }
}
