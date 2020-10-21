package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.google.protobuf.ByteString
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
        val (proposalBytes, _) = chaincodeConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the transaction to the ledger" in {
        val transactionName = "addCertificate"
        val enrollmentId = "102"
        val certificate = "Whatever"
        val (proposalBytes, transactionId) = chaincodeConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val transactionContext: TransactionContext = chaincodeConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        val result = chaincodeConnection.submitSignedProposal(proposalBytes, signature, transactionName, transactionId, enrollmentId, certificate)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + new String(result, StandardCharsets.UTF_8))
      }
    }

    "passing a VUE-signed transaction" should {
      "accept the transaction on the ledger" in {
        val transactionName = "addCertificate"
        val enrollmentId = "101"
        val certificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val (proposalBytes, transactionId) = chaincodeConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = chaincodeConnection.submitSignedProposal(proposalBytes, signature, transactionName, transactionId, enrollmentId, certificate)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + new String(result, StandardCharsets.UTF_8))
      }
    }
  }
}
