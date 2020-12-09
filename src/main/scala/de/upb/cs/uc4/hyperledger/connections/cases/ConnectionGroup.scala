package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionGroupTrait

protected[hyperledger] case class ConnectionGroup(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionGroupTrait {

  override def getProposalAddUserToGroup(enrollmentId: String, groupId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("addUserToGroup", enrollmentId, groupId)
  }

  override def getProposalRemoveUserFromGroup(enrollmentId: String, groupId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("removeUserFromGroup", enrollmentId, groupId)
  }

  override def getProposalRemoveUserFromAllGroups(enrollmentId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("removeUserFromAllGroups", enrollmentId)
  }

  override def getProposalGetAllGroups: Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getAllGroups")
  }

  override def getProposalGetUsersForGroup(groupId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getUsersForGroup", groupId)
  }

  override def getProposalGetGroupsForUser(enrollmentId: String): Array[Byte] = {
    // TODO: add error handling
    internalGetUnsignedProposal("getProposalGetGroupsForUser", enrollmentId)
  }

  override def addUserToGroup(enrollmentId: String, groupId: String): String =
    wrapSubmitTransaction(false, "addUserToGroup", enrollmentId, groupId)

  override def removeUserFromGroup(enrollmentId: String, groupId: String): String =
    wrapSubmitTransaction(false, "removeUserFromGroup", enrollmentId, groupId)

  override def removeUserFromAllGroups(enrollmentId: String): String =
    wrapSubmitTransaction(false, "removeUserFromAllGroups", enrollmentId)

  override def getAllGroups: String =
    wrapEvaluateTransaction("getAllGroups")

  override def getUsersForGroup(groupId: String): String =
    wrapEvaluateTransaction("getUsersForGroup", groupId)

  override def getGroupsForUser(enrollmentId: String): String =
    wrapEvaluateTransaction("getGroupsForUser", enrollmentId)
}
