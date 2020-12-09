package de.upb.cs.uc4.hyperledger.connections.traits

import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }

trait ConnectionGroupTrait extends ConnectionTrait {
  final override val contractName: String = "UC4.Admission"

  /** Retrieves a proposal for the designated query
    * Also submits the "addAdmission" query as current user (admin).
    *
    * @param enrollmentId Information about the enrollmentId.
    * @param groupId Information about the groupId.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalAddUserToGroup(enrollmentId: String, groupId: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits an approval for the transaction as the current user (admin).
    *
    * @param enrollmentId enrollmentId to remove.
    * @param groupId groupId from which user is removed.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalRemoveUserFromGroup(enrollmentId: String, groupId: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits the "dropAdmission" query as current user (admin).
    *
    * @param enrollmentId enrollmentId to remove.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalRemoveUserFromAllGroups(enrollmentId: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits the "getAdmissions" query as current user (admin).
    *
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetAllGroups: Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits an approval for the transaction as the current user (admin).
    *
    * @param groupId groupId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetUsersForGroup(groupId: String): Array[Byte]

  /** Retrieves a proposal for the designated query
    * Also submits an approval for the transaction as the current user (admin).
    *
    * @param enrollmentId enrollmentId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Proposal and transactionId
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getProposalGetGroupsForUser(enrollmentId: String): Array[Byte]

  /** Submits the "addUserToGroup" query.
    *
    * @param enrollmentId Information about the enrollmentId.
    * @param groupId Information about the groupId.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return ledger state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def addUserToGroup(enrollmentId: String, groupId: String): String

  /** Submits the "removeUserFromGroup" query.
    *
    * @param enrollmentId enrollmentId to remove.
    * @param groupId groupId from which user is removed.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def removeUserFromGroup(enrollmentId: String, groupId: String): String

  /** Submits the "removeUserFromAllGroups" query.
    *
    * @param enrollmentId enrollmentId to remove from all groups.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return Success_state
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def removeUserFromAllGroups(enrollmentId: String): String

  /** Submits the "getAllGroups" query.
    *
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getAllGroups: String

  /** Submits the "getUsersForGroup" query.
    *
    * @param groupId groupId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getUsersForGroup(groupId: String): String

  /** Submits the "getGroupsForUser" query.
    *
    * @param enrollmentId enrollmentId to filter for.
    * @throws TransactionExceptionTrait if chaincode throws an exception.
    * @throws HyperledgerExceptionTrait if hlf-framework throws an exception.
    * @return list of admissions matching the filters.
    */
  @throws[HyperledgerExceptionTrait]
  @throws[TransactionExceptionTrait]
  def getGroupsForUser(enrollmentId: String): String

}
