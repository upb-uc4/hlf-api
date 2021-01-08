package de.upb.cs.uc4.hyperledger.tests

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.util.Base64

import de.upb.cs.uc4.hyperledger.connections.traits.ConnectionAdmissionTrait
import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionCertificateTrait, ConnectionMatriculationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.tests.testUtil.{ TestDataAdmission, TestDataMatriculation, TestHelper, TestHelperCrypto, TestHelperStrings, TestSetup }
import de.upb.cs.uc4.hyperledger.utilities.helper.Logger
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.security.CryptoPrimitives

class UnsignedTransactionTests extends TestBase {

  val crypto: CryptoPrimitives = TestHelperCrypto.getCryptoPrimitives
  var certificateConnection: ConnectionCertificateTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _
  var admissionConnection: ConnectionAdmissionTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
    admissionConnection = initializeAdmission()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
    TestSetup.establishExistingMatriculation(initializeMatriculation(), "701")
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  private def prepareUser(userName: String): (PrivateKey, String) = {
    Logger.info(s"prepare User:: $userName")
    // get testUser certificate and private key
    val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(userName)
    val privateKey: PrivateKey = testUserIdentity.getPrivateKey
    val certificatePem: String = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)

    (privateKey, certificatePem)
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned proposal" should {
      "return an unsigned proposal" in {
        val testUserId = "frontend-signing-tester-updateCertTest-success"
        val (_, certificate) = prepareUser(testUserId)
        val (_, proposalBytes) = certificateConnection.getProposalUpdateCertificate(certificate, testUserId, certificate)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        val header = proposal.getHeader.toStringUtf8

        // header contains signing user certificate
        header should include(certificate)

        // payload contains Approval TransactionInfo
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(
          "UC4.Certificate",
          "updateCertificate",
          testUserId,
          certificate
        ))
      }
    }
    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val testUserId = "frontend-signing-tester-updateCertTest-denyCert"
        val (privateKey, _) = prepareUser(testUserId)

        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val (_, proposalBytes) = certificateConnection.getProposalUpdateCertificate(wrongCertificate, testUserId, wrongCertificate)

        // fake signature for given certificate
        val signature: Array[Byte] = crypto.sign(privateKey, proposalBytes)

        // try use signature
        val result = intercept[HyperledgerExceptionTrait](certificateConnection.getUnsignedTransaction(proposalBytes, signature))
        result.actionName should be("validatePeerResponses")
      }
    }
    "getting a proposal for a faulty transaction " should {
      "deny getting the proposal and throw a matching exception" in {
        val enrollmentId = "frontend-signing-tester-updateCertTest-denyTransaction"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val exception = intercept[TransactionExceptionTrait](certificateConnection.getProposalAddCertificate(certificate, enrollmentId, certificate))
        exception.transactionName should be("addCertificate")
        exception.payload should include("HLConflict")
      }
    }
  }

  "The ConnectionMatriculation" when {
    "passing a signed transaction" should {
      "submit the approval transaction to the approval contract" in {
        val testUserId = "frontend-signing-tester-7"
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
        // val getResult = initializeOperation(username).getOperationData(transactionApprovalResult)
        //getResult should include(testUserId)
        //getResult should include(username)
      }
      "submit the real transaction to the real contract" in {
        val testUserId = "frontend-signing-tester2"
        val (privateKey, certificate) = prepareUser(testUserId)

        // prepare test data
        val testMatData = TestDataMatriculation.validMatriculationData1(testUserId)

        // get proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(
          certificate,
          jSonMatriculationData = testMatData
        )
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
        // val getResult = initializeOperation(username).getApprovals("UC4.MatriculationData", "addMatriculationData", testMatData)
        //getResult should include(testUserId)
        //getResult should include(username)

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
        val (_, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(certificate, inputMatJSon)
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
        matriculationConnection.addMatriculationData(inputMatJSon)

        // Log proposal
        val (_, proposalBytes) = matriculationConnection.getProposalUpdateMatriculationData(certificate, inputMatJSon)
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
        matriculationConnection.addMatriculationData(inputMatJSon)

        // Log proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddEntriesToMatriculationData(certificate, testUserId,
          TestDataMatriculation.validMatriculationEntry)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = matriculationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataTransaction:: $transactionInfo")
      }
    }

    "preparing data for admissions " should {
      "print info for addAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputAdmissionJson = TestDataAdmission.validAdmission(testUserId, "C1", "MatriculationTestModule.1", "2020-12-31T23:59:59")
        val matriculationData = TestDataMatriculation.validMatriculationData4(testUserId)
        matriculationConnection.addMatriculationData(matriculationData)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalAddAdmission(certificate, inputAdmissionJson)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = admissionConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddAdmissionTransaction:: $transactionInfo")
      }
      "print info for dropAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputAdmissionJson = TestDataAdmission.validAdmission(testUserId, "C1", "MatriculationTestModule.1", "2020-12-31T23:59:59")
        admissionConnection.addAdmission(inputAdmissionJson)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalDropAdmission(certificate, testUserId + ":C1")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"DropAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = admissionConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"DropAdmissionTransaction:: $transactionInfo")
      }
      "print info for getAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalGetAdmission(certificate, testUserId, "C1", "MatriculationTestModule.1")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"GetAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = admissionConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"GetAdmissionTransaction:: $transactionInfo")
      }
    }
  }

}
