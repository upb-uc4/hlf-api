package de.upb.cs.uc4.hyperledger.tests.general

import java.nio.charset.StandardCharsets
import java.util.Base64

import de.upb.cs.uc4.hyperledger.connections.traits.{ ConnectionAdmissionTrait, ConnectionCertificateTrait, ConnectionMatriculationTrait, ConnectionOperationTrait }
import de.upb.cs.uc4.hyperledger.exceptions.traits.{ HyperledgerExceptionTrait, TransactionExceptionTrait }
import de.upb.cs.uc4.hyperledger.testBase.TestBase
import de.upb.cs.uc4.hyperledger.testUtil._
import de.upb.cs.uc4.hyperledger.utilities.helper.{ Logger, StringHelper }
import org.hyperledger.fabric.gateway.impl.identity.X509IdentityImpl
import org.hyperledger.fabric.protos.peer.ProposalPackage.Proposal
import org.hyperledger.fabric.sdk.security.CryptoPrimitives

class UnsignedTransactionTests extends TestBase {

  val crypto: CryptoPrimitives = TestHelperCrypto.getCryptoPrimitives
  var certificateConnection: ConnectionCertificateTrait = _
  var operationConnection: ConnectionOperationTrait = _
  var matriculationConnection: ConnectionMatriculationTrait = _
  var admissionConnection: ConnectionAdmissionTrait = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    certificateConnection = initializeCertificate()
    matriculationConnection = initializeMatriculation()
    admissionConnection = initializeAdmission()
    operationConnection = initializeOperation()
    TestSetup.establishExaminationRegulations(initializeExaminationRegulation())
    tryRegisterAndEnrollTestUser("701", organisationId)
    TestSetup.establishExistingMatriculation(initializeMatriculation(), initializeOperation("701"), "701")
  }

  override def afterAll(): Unit = {
    certificateConnection.close()
    matriculationConnection.close()
    super.afterAll()
  }

  "The ConnectionCertificate" when {
    "querying for an unsigned proposal" should {
      "return an unsigned proposal" in {
        val testUserId = "frontend-signing-tester-updateCertTest-success"
        val (_, certificate) = prepareUser(testUserId)
        val jsonRegulation = TestDataExaminationRegulation.validExaminationRegulation("UnsignedTransactionTests_ComputerScience", Seq("M1"), true)
        val (adminApproval, proposalBytes) = initializeExaminationRegulation().getProposalAddExaminationRegulation(certificate, organisationId, jsonRegulation)
        val proposal: Proposal = Proposal.parseFrom(proposalBytes)
        val header = proposal.getHeader.toStringUtf8

        // header contains signing user certificate
        header should include(certificate)

        val operationId = StringHelper.getOperationIdFromOperation(adminApproval)
        // payload contains Approval TransactionInfo
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(operationId))
      }
    }
    "passing a wrongly-signed transaction" should {
      "deny the transaction on the ledger" in {
        val testUserId = "frontend-signing-tester-updateCertTest-denyCert"
        val (privateKey, _) = prepareUser(testUserId)

        val wrongCertificate =
          "-----BEGIN CERTIFICATE-----\nMIICxjCCAm2gAwIBAgIUGJFrzMxyOAdnJErfr+UfDrLDJb4wCgYIKoZIzj0EAwIw\nYDELMAkGA1UEBhMCVVMxFzAVBgNVBAgTDk5vcnRoIENhcm9saW5hMRQwEgYDVQQK\nEwtIeXBlcmxlZGdlcjEPMA0GA1UECxMGRmFicmljMREwDwYDVQQDEwhyY2Etb3Jn\nMTAeFw0yMDEwMjAxMDEzMDBaFw0yMTEwMjAxMDE4MDBaMDgxDjAMBgNVBAsTBWFk\nbWluMSYwJAYDVQQDEx1zY2FsYS1yZWdpc3RyYXRpb24tYWRtaW4tb3JnMTBZMBMG\nByqGSM49AgEGCCqGSM49AwEHA0IABLStxuihhyb2XU0wzMhV3Su2Dr7LUI4z/IeL\nzeUDzhcqnZxLDN5w43rV0FXu4yRq0krOaxRhpAY65dmQQ6PRrzujggErMIIBJzAO\nBgNVHQ8BAf8EBAMCA6gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMAwG\nA1UdEwEB/wQCMAAwHQYDVR0OBBYEFLAa99vOXhJylch+MQGthFCG/v+RMB8GA1Ud\nIwQYMBaAFBJ7z3hS1NU4HpEaFgyWKir699s5MCgGA1UdEQQhMB+CHXNjYWxhLXJl\nZ2lzdHJhdGlvbi1hZG1pbi1vcmcxMH4GCCoDBAUGBwgBBHJ7ImF0dHJzIjp7ImFk\nbWluIjoidHJ1ZSIsImhmLkFmZmlsaWF0aW9uIjoiIiwiaGYuRW5yb2xsbWVudElE\nIjoic2NhbGEtcmVnaXN0cmF0aW9uLWFkbWluLW9yZzEiLCJoZi5UeXBlIjoiYWRt\naW4ifX0wCgYIKoZIzj0EAwIDRwAwRAIgEjWf7bQyGkHf2bj16MyQ874wCWOb8l2M\n60MlJ4eDgosCIEbD4+stNqZKKsJ+C48IerpOJD3jwkLG+8y7YuxTpx8Z\n-----END CERTIFICATE-----\n"
        val jsonRegulation = TestDataExaminationRegulation.validExaminationRegulation("UnsignedTransactionTests_ComputerScience", Seq("M1"), true)
        val (_, proposalBytes) = initializeExaminationRegulation().getProposalAddExaminationRegulation(wrongCertificate, organisationId, jsonRegulation)

        // fake signature for given certificate
        val signature: Array[Byte] = crypto.sign(privateKey, proposalBytes)

        // try use signature
        val result = intercept[HyperledgerExceptionTrait](operationConnection.getUnsignedTransaction(proposalBytes, signature))
        result.actionName should be("validatePeerResponses")
      }
    }
    "getting a proposal for a faulty transaction " should {
      "deny getting the proposal and throw a matching exception" in {
        val enrollmentId = "frontend-signing-tester-updateCertTest-denyTransaction"
        val testUserIdentity: X509IdentityImpl = tryRegisterAndEnrollTestUser(enrollmentId, organisationId)
        val certificate = TestHelperCrypto.toPemString(testUserIdentity.getCertificate)
        val exception = intercept[TransactionExceptionTrait](certificateConnection.getProposalAddCertificate(
          certificate,
          organisationId, enrollmentId, certificate
        ))
        exception.transactionName should be("initiateOperation")
        exception.payload should include("HLConflict")
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
          organisationId,
          jSonMatriculationData = testMatData
        )

        val operationId = StringHelper.getOperationIdFromOperation(proposalApprovalResult)
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(operationId))
        proposalApprovalResult should include(username)

        // get transaction for signature
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(transactionBytes, Seq(operationId))

        // sign transaction and submit transaction
        val transactionApprovalResult = operationConnection.submitSignedTransaction(
          transactionBytes, crypto.sign(privateKey, transactionBytes)
        )
        val transactionResult = operationConnection.executeTransaction(transactionApprovalResult)

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
        val (proposalApprovalResult, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(
          certificate,
          organisationId,
          jSonMatriculationData = testMatData
        )
        val operationId = StringHelper.getOperationIdFromOperation(proposalApprovalResult)
        TestHelper.testProposalPayloadBytesContainsInfo(proposalBytes, Seq(operationId))

        // get transaction for signature
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(
          proposalBytes,
          crypto.sign(privateKey, proposalBytes)
        )
        TestHelper.testTransactionBytesContainsInfo(transactionBytes, Seq(operationId))

        // sign transaction and submit transaction
        val transactionApprovalResult = operationConnection.submitSignedTransaction(
          transactionBytes,
          crypto.sign(privateKey, transactionBytes)
        )
        val realTransactionResult = operationConnection.executeTransaction(transactionApprovalResult)

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
    "preparing data for matriculation approve " should {
      "print info for addMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData3(testUserId)

        // Log proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddMatriculationData(certificate, organisationId, inputMatJSon)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataApprovalProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataApprovalTransaction:: $transactionInfo")
      }
      "print info for updateMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-updateMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4(testUserId)
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.MatriculationData", "addMatriculationData", inputMatJSon)
        matriculationConnection.addMatriculationData(inputMatJSon)

        // Log proposal
        val (_, proposalBytes) = matriculationConnection.getProposalUpdateMatriculationData(certificate, organisationId, inputMatJSon)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataApprovalProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataApprovalTransaction:: $transactionInfo")
      }
      "print info for addEntriesToMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addEntriesToMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4(testUserId)
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.MatriculationData", "addMatriculationData", inputMatJSon)
        matriculationConnection.addMatriculationData(inputMatJSon)

        // Log proposal
        val (_, proposalBytes) = matriculationConnection.getProposalAddEntriesToMatriculationData(certificate, organisationId, testUserId,
          TestDataMatriculation.validMatriculationEntry)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataApprovalProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataApprovalTransaction:: $transactionInfo")
      }
    }

    "preparing data for matriculation reject " should {
      "print info for addMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData3(testUserId)

        // initial approve
        val operationData = operationConnection.initiateOperation(
          testUserId, "UC4.MatriculationData", "addMatriculationData", inputMatJSon
        )
        val id = TestHelperStrings.getOperationIdFromOperationData(operationData)

        // Log proposal
        val proposalBytes = operationConnection.getProposalRejectOperation(certificate, organisationId, id, "ExampleRejectMessage")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataRejectionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddMatriculationDataRejectionTransaction:: $transactionInfo")
      }
      "print info for updateMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-updateMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputMatJSon = TestDataMatriculation.validMatriculationData4(testUserId)

        // initial approve
        val operationData = operationConnection.initiateOperation(
          testUserId, "UC4.MatriculationData", "updateMatriculationData", inputMatJSon
        )
        val id = TestHelperStrings.getOperationIdFromOperationData(operationData)

        // Log proposal
        val proposalBytes = operationConnection.getProposalRejectOperation(certificate, organisationId, id, "ExampleRejectMessage")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataRejectionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"UpdateMatriculationDataRejectionTransaction:: $transactionInfo")
      }
      "print info for addEntriesToMatriculationData" in {
        val testUserId = "frontend-signing-tester-info-addEntriesToMatriculationData"
        val (privateKey, certificate) = prepareUser(testUserId)

        // initial approve
        val operationData = operationConnection.initiateOperation(
          testUserId,
          "UC4.MatriculationData",
          "addEntriesToMatriculationData",
          testUserId,
          TestDataMatriculation.validMatriculationEntry
        )
        val id = TestHelperStrings.getOperationIdFromOperationData(operationData)

        // Log proposal
        val proposalBytes = operationConnection.getProposalRejectOperation(certificate, organisationId, id, "ExampleRejectMessage")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataRejectionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddEntriesToMatriculationDataRejectionTransaction:: $transactionInfo")
      }
    }

    "preparing data for admissions " should {
      "print info for addAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputAdmissionJson = TestDataAdmission.validAdmission(testUserId, "C1", "MatriculationTestModule.1", "2020-12-31T23:59:59")
        val matriculationData = TestDataMatriculation.validMatriculationData4(testUserId)
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.MatriculationData", "addMatriculationData", matriculationData)
        matriculationConnection.addMatriculationData(matriculationData)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalAddAdmission(certificate, organisationId, inputAdmissionJson)
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"AddAdmissionTransaction:: $transactionInfo")
      }
      "print info for dropAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)
        val inputAdmissionJson = TestDataAdmission.validAdmission(testUserId, "C1", "MatriculationTestModule.1", "2020-12-31T23:59:59")
        initializeOperation(testUserId).initiateOperation(testUserId, "UC4.Admission", "addAdmission", inputAdmissionJson)
        admissionConnection.addAdmission(inputAdmissionJson)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalDropAdmission(certificate, organisationId, testUserId + ":C1")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"DropAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"DropAdmissionTransaction:: $transactionInfo")
      }
      "print info for getAdmission" in {
        val testUserId = "frontend-signing-tester-info-admission"
        val (privateKey, certificate) = prepareUser(testUserId)

        // Log proposal
        val (_, proposalBytes) = admissionConnection.getProposalGetAdmission(certificate, organisationId, testUserId, "C1", "MatriculationTestModule.1")
        val proposalInfo = new String(Base64.getEncoder.encode(proposalBytes), StandardCharsets.UTF_8)
        Logger.debug(s"GetAdmissionProposal:: $proposalInfo")

        // Log transaction
        val transactionBytes: Array[Byte] = operationConnection.getUnsignedTransaction(proposalBytes, crypto.sign(privateKey, proposalBytes))
        val transactionInfo = new String(Base64.getEncoder.encode(transactionBytes), StandardCharsets.UTF_8)
        Logger.debug(s"GetAdmissionTransaction:: $transactionInfo")
      }
    }
  }

}
