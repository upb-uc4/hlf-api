package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionMatriculationTrait
import de.upb.cs.uc4.hyperledger.exceptions.traits.{HyperledgerExceptionTrait, TransactionExceptionTrait}
import de.upb.cs.uc4.hyperledger.utilities.ConnectionManager

case class ConnectionMatriculation(id: String, channel: String, chaincode: String, wallet_path: Path, network_description_path: Path) extends ConnectionMatriculationTrait {

  val contract_name: String = "UC4.MatriculationData"
  val (contract, gateway) = ConnectionManager.initializeConnection(id, channel, chaincode, this.contract_name, network_description_path, wallet_path)

  /**
   * Executes the "addCourse" query.
   *
   * @param jSonMatriculationData Information about the matriculation to add.
   * @throws Exception if chaincode throws an exception.
   * @return Success_state
   */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  override def addMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult("addMatriculationData",
      this.internalSubmitTransaction("addMatriculationData", jSonMatriculationData))

  /**
   * Submits the "deleteCourseById" query.
   *
   * @param matriculationId courseId to add entry to
   * @param fieldOfStudy    field of study the student enrolled in
   * @param semester        the semester the student enrolled for
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  override def addEntryToMatriculationData(matriculationId: String, fieldOfStudy: String, semester: String): String =
    wrapTransactionResult("addEntryToMatriculationData",
      this.internalSubmitTransaction("addEntryToMatriculationData", matriculationId, fieldOfStudy, semester))

  /**
   * Submits the "updateCourseById" query.
   *
   * @param jSonMatriculationData matriculationInfo to update
   * @throws Exception if chaincode throws an exception.
   * @return success_state
   */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  override def updateMatriculationData(jSonMatriculationData: String): String =
    wrapTransactionResult("updateMatriculationData",
      this.internalSubmitTransaction("updateMatriculationData", jSonMatriculationData))
  /**
   * Executes the "getCourseById" query.
   *
   * @param matId matriculationId to get information
   * @throws Exception if chaincode throws an exception.
   * @return JSon Course Object
   */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  override def getMatriculationData(matId: String): String =
    wrapTransactionResult("getMatriculationData",
      this.internalEvaluateTransaction("getMatriculationData", matId))
}