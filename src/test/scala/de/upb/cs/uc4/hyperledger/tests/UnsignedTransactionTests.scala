package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.google.protobuf.ByteString
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.TestDataMatriculation
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.transaction.TransactionContext

class UnsignedTransactionTests extends TestBase {

  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned transaction" should {
      "return an unsigned transaction" in {
        val enrollmentId = "100"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
      }
    }

    "passing a signed transaction" should {
      "submit the proposal transaction to the proposal contract" in {
        val enrollmentId = "102"
        val certificate = "Whatever"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nPROPOSALBYTES:\n##########################\n\n" + proposal.toByteString.toStringUtf8)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        val b64Sig = ByteString.copyFrom(Base64.getEncoder.encode(signature.toByteArray)).toStringUtf8
        println("\n\n\n##########################\nSignature:\n##########################\n\n" + b64Sig)
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult:\n##########################\n\n" + result)
      }
      "submit the real transaction to the real contract" in {
        // store info
        val enrollmentId = "103"
        val certificate = "Whatever"
        println("\n\n\n##########################\nGET PROPOSAL:\n##########################\n\n")
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, certificate)
        val transactionContext: TransactionContext = certificateConnection.contract.getNetwork.getChannel.newTransactionContext()
        val signature = transactionContext.signByteString(proposalBytes)
        println("\n\n\n##########################\nSUBMIT PROPOSAL:\n##########################\n\n")
        val result = certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray)
        println("\n\n\n##########################\nResult103:\n##########################\n\n" + result)

        // test info stored
        val storedCert = certificateConnection.getCertificate(enrollmentId)
        storedCert should be(certificate)
      }
    }

    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val approvalTransactionName = "approveTransaction"
        val enrollmentId = "101"
        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val proposalBytes = certificateConnection.getProposalAddCertificate(enrollmentId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        println("\n\n\n##########################\nHeader:\n##########################\n\n" + proposal.getHeader.toStringUtf8)
        println("\n\n\n##########################\nPayload:\n##########################\n\n" + proposal.getPayload.toStringUtf8)
        val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.submitSignedProposal(proposalBytes, signature.toByteArray))
        result.actionName should be(approvalTransactionName)
      }
    }

    "testing info" should {
      "not fail 1" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData3("500")
        val proposalBytes = matriculationConnection.getProposalAddMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $info")
      }
      "not fail 2" in {
        val inputMatJSon = TestDataMatriculation.validMatriculationData4("500")
        val proposalBytes = matriculationConnection.getProposalUpdateMatriculationData(inputMatJSon)
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $info")
      }
      "not fail 3" in {
        val proposalBytes = matriculationConnection.getProposalAddEntriesToMatriculationData(
          "500",
          TestDataMatriculation.validMatriculationEntry
        )
        val info = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataProposal:: $info")
      }
    }
  }
}
