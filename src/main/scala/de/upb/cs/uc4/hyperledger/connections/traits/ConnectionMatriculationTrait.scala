package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionMatriculationTrait extends ConnectionTrait {

  /** Executes the "addMatriculationData" query.
    *
    * @param jSonMatriculationData Information about the matriculation to add.
    * @throws Exception if chaincode throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addMatriculationData(jSonMatriculationData: String): String

  /** Submits the "addEntryToMatriculationData" query.
    *
    * @param matriculationId matriculationId to add entry to
    * @param fieldOfStudy    field of study the student enrolled in
    * @param semester        the semester the student enrolled for
    * @throws Exception if chaincode throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addEntryToMatriculationData(matriculationId: String, fieldOfStudy: String, semester: String): String

  /** Submits the "updateMatriculationData" query.
    *
    * @param jSonMatriculationData matriculationInfo to update
    * @throws Exception if chaincode throws an exception.
    * @return success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def updateMatriculationData(jSonMatriculationData: String): String

  /** Executes the "getMatriculationData" query.
    *
    * @param matId matriculationId to get information
    * @throws Exception if chaincode throws an exception.
    * @return JSon Course Object
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getMatriculationData(matId: String): String
}
