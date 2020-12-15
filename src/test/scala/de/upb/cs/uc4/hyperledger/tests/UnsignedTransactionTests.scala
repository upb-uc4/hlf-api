package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.util.Base64

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.HyperledgerExceptionTrait
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataMatriculation, TestHelper, TestHelperCrypto, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.security.CryptoPrimitives

class UnsignedTransactionTests extends TestBase {

  val crypto: CryptoPrimitives = TestHelperCrypto.getCryptoPrimitives
  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  private def prepareUser(userName: String): (PrivateKey, String) = {
    Logger.info(s"prepare User:: $userName")
    // get testUser certificate and private key
    val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(userName, organisationId)
    val privateKey: PrivateKey = testUserIdentity.getPrivateKey
    val certificatePem: String = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)

    (privateKey, certificatePem)
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned proposal" should {
      "return an unsigned proposal" in {
        val testUserId = "100"
        val (privateKey, certificate) = prepareUser(testUserId)
        val (approvalResult, proposalBytes) = certificateConnection.getProposalAddCertificate(certificate, organisationId, testUserId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        val header = proposal.getHeader.toStringUtf8

        // header contains signing user certificate
        header should include(certificate)

        // payload contains Approval TransactionInfo
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(
          "UC4.Certificate",
          "addCertificate",
          testUserId,
          certificate
        ))
      }
    }
    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val testUserId = "101"
        val (privateKey, certificate) = prepareUser(testUserId)

        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val (approvalResult, proposalBytes) = certificateConnection.getProposalUpdateCertificate(wrongCertificate, organisationId, testUserId, wrongCertificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)

        // fake signature
        val signature = crypto.sign(privateKey, proposalBytes)
        // val signature = ByteString.copyFrom(Base64.getDecoder.decode("MEUCIQD92OsJsVVFqFfifMV14ROiL5Ni/RaOBkR0DqzetvPfkQIgcrgu9vxr5TuZY6lft5adCETaC3CSE8QA+bs9MheeLcI="))

        // try use signature
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.getUnsignedTransaction(proposalBytes, signature))
        result.actionName should be("validatePeerResponses")
      }
    }
  }

  "The ConnectionMatriculation" when {
    "passing a signed transaction" should {
      "submit the approval transaction to the approval contract" in {
        val testUserId = "frontend-signing-tester"
        val (privateKey, certificate) = prepareUser(testUserId)

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(testUserId)

        // get proposal
        val (proposalApprovalResult, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(
          certificate,
          jSonMatriculationData = testMatData
        )
        TestHelper.testProposalPayloadBytesContainsInfo(
          proposalBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )
        proposalApprovalResult should include(username)

        // get transaction for signature
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(
          transactionBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // sign transaction and submit transaction
        val (transactionApprovalResult, transactionResult) = matriculationConnection.submitSignedTransaction(
          transactionBytes, crypto.sign(privateKey, transactionBytes)
        )
        transactionApprovalResult should include(testUserId)
        transactionApprovalResult should include(username)
        TestHelperStrings.compareJson(testMatData, transactionResult)

        // test approvals on ledger
        val getResult = initializeApproval(username).getApprovals("UC4.MatriculationData", "addMatriculationData", testMatData)
        getResult should include(testUserId)
        getResult should include(username)
      }
      "submit the real transaction to the real contract" in {
        val testUserId = "frontend-signing-tester2"
        val (privateKey, certificate) = prepareUser(testUserId)

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(testUserId)

        // get proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(
          certificate,
          jSonMatriculationData = testMatData)
        TestHelper.testProposalPayloadBytesContainsInfo(
          proposalBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // get transaction for signature
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(
          transactionBytes,
          Seq("UC4.MatriculationData", "addMatriculationData", testMatData)
        )

        // sign transaction and submit transaction
        val (transactionApprovalResult, realTransactionResult) = matriculationConnection.submitSignedTransaction(
          transactionBytes,
          crypto.sign(privateKey, transactionBytes)
        )
        // check real result
        TestHelperStrings.compareJson(testMatData, realTransactionResult)
        // check approvalResult
        transactionApprovalResult should include(testUserId)
        transactionApprovalResult should include(username)

        // test approvals on ledger
        val getResult = initializeApproval(username).getApprovals("UC4.MatriculationData", "addMatriculationData", testMatData)
        getResult should include(testUserId)
        getResult should include(username)

        // test info stored on matriculation ledger
        val storedMatData = matriculationConnection.getMatriculationData(testUserId)
        TestHelperStrings.compareJson(testMatData, storedMatData)
      }
    }
  }

  "PrintTest info " when {
    "preparing data for matriculation " should {
      "print info for addMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData3(testUserId)

        // Log proposal
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(certificate, organisationId, inputMatJSon)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataTransaction:: $transactionInfo")
      }
      "print info for updateMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-updateMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4(testUserId)

        // Log proposal
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalUpdateMatriculationData(certificate, organisationId, inputMatJSon)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataTransaction:: $transactionInfo")
      }
      "print info for addEntriesToMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addEntriesToMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4(testUserId)

        // Log proposal
        val (approvalResult, proposalBytes) = matriculationConnection.getProposalAddEntriesToMatriculationData(certificate, organisationId, testUserId,
          TestDataMatriculation.validMatriculationEntry)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataTransaction:: $transactionInfo")
      }
    }
  }
  
}
