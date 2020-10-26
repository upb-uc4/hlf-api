package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionCertificateTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.transaction.TransactionContext

class UnsignedTransactionTests extends TestBase {

  var chaincodeConnection: ConnectionCertificateTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    chaincodeConnection = initializeCertificate()
  }

  override def afterAll(): Unit = {
    chaincodeConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned transaction" should {
      "return an unsigned transaction" in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        val proposalBytes: Array[Byte] = chaincodeConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the transaction to the ledger" in {
        val enrollmentId = "101"
        val certificate = "Whatever"
        val proposalBytes = chaincodeConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val transactionContext: TransactionContext = chaincodeConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        val result = chaincodeConnection.submitSignedProposal(proposalBytes, signature)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + new String(result, StandardCharsets.UTF_8))
      }
    }
  }
}
