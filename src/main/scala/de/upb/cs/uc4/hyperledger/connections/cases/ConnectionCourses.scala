package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCourseTrait
import de.upb.cs.uc4.hyperledger.exceptions.TransactionException
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

class ConnectionCourses(id: String, channel: String, chaincode: String, wallet_path: Path, network_description_path: Path) extends ConnectionCourseTrait {
  final val contract_name: String = "UC4.course"
  override val (contract, gateway) = ConnectionManager.initializeConnection(id, channel, chaincode, this.contract_name, wallet_path, network_description_path)

  override def addCourse(jSonCourse: String): String =
    this.customWrapTransactionResult("addCourse", this.internalSubmitTransaction("addCourse", jSonCourse))

  override def deleteCourseById(courseId: String): String =
    this.customWrapTransactionResult("deleteCourseById", this.internalSubmitTransaction("deleteCourseById", courseId))

  override def updateCourseById(courseId: String, jSonCourse: String): String =
    this.customWrapTransactionResult("updateCourseById", this.internalSubmitTransaction("updateCourseById", courseId, jSonCourse))

  override def getAllCourses: String =
    this.customWrapTransactionResult("getAllCourses", this.internalEvaluateTransaction("getAllCourses"))

  override def getCourseById(courseId: String): String = {
    val result = this.customWrapTransactionResult("getCourseById", this.internalEvaluateTransaction("getCourseById", courseId))

    // check specific error
    if (result == "null") throw TransactionException.CreateUnknownException("getCourseById", "Returned null.")
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
  @throws[TransactionException]
  private def customWrapTransactionResult(transactionId: String, result: Array[Byte]): String = {
    val resultString = convertTransactionResult(result)
    if (containsError(resultString)) throw extractErrorFromResult(transactionId, resultString)
    else resultString
  }

  /**
   *  gets the CUSTOM course errors and wraps them as a TransactionException
   * @param transactionId
   * @param result
   * @return
   */
  @throws[TransactionException]
  private def extractErrorFromResult(transactionId: String, result: String): TransactionException = {
    // retrieve error code
    var id = result.substring(result.indexOf("\"name\":\"") + 8)
    id = id.substring(0, id.indexOf("\""))

    // retrieve detail
    var detail = result.substring(result.indexOf("\"detail\":\"") + 10)
    detail = detail.substring(0, detail.indexOf("\""))

    // create Exception
    TransactionException.CreateUnknownException(transactionId, detail)
  }

  /**
   * Evaluates whether a COURSE transaction was valid or invalid
   *
   * @param result result of a chaincode transaction
   * @return true if the result contains error information
   */
  private def containsError(result: String): Boolean = {
    result.contains("{\"name\":") && result.contains("\"detail\":")
  }
}