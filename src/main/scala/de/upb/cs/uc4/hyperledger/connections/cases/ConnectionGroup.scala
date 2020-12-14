package de.upb.cs.uc4.hyperledger.connections.cases

import java.nio.file.Path

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionGroupTrait

protected[hyperledger] case class ConnectionGroup(username: String, channel: String, chaincode: String, walletPath: Path, networkDescriptionPath: Path)
  extends ConnectionGroupTrait {

  override def getProposalAddUserToGroup(certificate: String, affiliation: String = AFFILITATION, enrollmentId: String, groupId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "addUserToGroup", enrollmentId, groupId)
  }

  override def getProposalRemoveUserFromGroup(certificate: String, affiliation: String = AFFILITATION, enrollmentId: String, groupId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "removeUserFromGroup", enrollmentId, groupId)
  }

  override def getProposalRemoveUserFromAllGroups(certificate: String, affiliation: String = AFFILITATION, enrollmentId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "removeUserFromAllGroups", enrollmentId)
  }

  override def getProposalGetAllGroups(certificate: String, affiliation: String = AFFILITATION): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getAllGroups")
  }

  override def getProposalGetUsersForGroup(certificate: String, affiliation: String = AFFILITATION, groupId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getUsersForGroup", groupId)
  }

  override def getProposalGetGroupsForUser(certificate: String, affiliation: String = AFFILITATION, enrollmentId: String): (String, Array[Byte]) = {
    // TODO: add error handling
    internalGetUnsignedProposal(certificate, affiliation, "getGroupsForUser", enrollmentId)
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
