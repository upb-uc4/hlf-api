# <a id="v0.10.2" />[v0.10.2 - WIP](https://github.com/upb-uc4/hyperledger_api/compare/v0.10.1...develop) (TBD)

## Feature

- 

## Bug Fixes

- 

## Refactor

- 

## Usability

- 

# <a id="v0.10.1" />[v0.10.1](https://github.com/upb-uc4/hyperledger_api/compare/v0.10.0...v0.10.1) (2020-10-20)

## Feature

- Provide first draft of an API for unsigned Transactions
- Provide Version Endpoint

## Refactor

- Test Structure CleanUp
- ConenctionTrait CleanUp

# <a id="v0.10.0" />[v0.10.0](https://github.com/upb-uc4/hyperledger_api/compare/v0.9.2...v0.10.0) (2020-10-16)

## Feature

- 

## Bug Fixes

- support multiple enrollments by updating existing certificate entries in case they already exist

# <a id="v0.9.2" />[v0.9.2](https://github.com/upb-uc4/hyperledger_api/compare/v0.9.1...v0.9.2) (2020-10-13)

## Usability

- Released on Maven via local push (import as follows)
```scala
libraryDependencies += "de.upb.cs.uc4" % "hlf_api" % "v0.9.2"
```


# <a id="v0.9.1" />[v0.9.1](https://github.com/upb-uc4/hyperledger_api/compare/v0.9.0...v0.9.1) (2020-10-02)

## Feature

- Support CertificateChaincode
  - addCertificate
  - getCertificate
  - updateCertificate
- Extend Enrollment to add the new Certificate to the Certificate Chaincode
- Throw TransactionExceptions instead of HyperledgerInnerException for "null"-parameters passed to transactions
- Throw NetworkExceptions instead of regular ones, if the network could not be reached or something with the EnrollmentProcess went wrong
- Add interfaces for public Managers to enable mocking

## Bug Fixes

- Improve resilience of parallel tests against negative influence from other tests

## Refactor

- Test cleanup
- Exception cleanup

## Usability

- CertificateAccessTests
- CertificateErrorTests
- Update UserManagement Tests
- Provide SequentialTestSuite
- NonExistingNetworkTests + Pipeline

# <a id="v0.9.0" />[v0.9.0](https://github.com/upb-uc4/hyperledger_api/compare/v0.8.3...v0.9.0) (2020-09-29)

## Feature

- Support Enrollment with CSR creating returning the correct SignedCertificate created from the CSR.

## Refactor

- Test cleanup

## Usability

- make production pipeline configurable regarding the chaincode-target

# <a id="v0.8.3" />[v0.8.3](https://github.com/upb-uc4/hyperledger_api/compare/v0.8.2...v0.8.3) (2020-09-28)

## Feature

- support new chaincode API (non-transient, enrollmentId instead of matriculationID)
- update Exception messaging for easier handling

## Usability

- remove firstname/lastname/birthdate from testData
- remove firstname/lastname/birthdate - error tests


switch transient / non-transient

# <a id="v0.8.2" />[v0.8.2](https://github.com/upb-uc4/hyperledger_api/compare/v0.8.1...v0.8.2) (2020-08-17)

## Feature

- Make userType and maxEnrollmentCount variable
- document publicly available methods
- Publish dedicated wallet Actions

## Bug Fixes

- hide services that are not meant to be accessible to the outside

## Refactor

- 

## Usability

- provide/use a logger for framework debugging

# <a id="v0.8.1" />[v0.8.1](https://github.com/upb-uc4/hyperledger_api/compare/v0.8.0...v0.8.1) (2020-08-17)

## Feature

- Support "Register User"

## Refactor

- Extract the CaClientManagement from the EnrollmentManager.

## Usability

- Pipelines can be triggered manually.
- Pipelines can be triggered for specific chaincode branches.
- Pipelines can be triggered for specific tests.


# <a id="v0.8.0" />[v0.8.0](https://github.com/upb-uc4/hyperledger_api/compare/v0.6.1...v0.8.0) (2020-08-17)

## Feature

- support new generation of chaincode (private data collection)
- support new generation of chaincode (multiple inserts at addEntriesToMatriculationData)

## Refactor

- camelCase
- rename repository to match standard

## Usability

- provide means to execute tests on different networks
- add test execution for [Production Network](https://github.com/upb-uc4/hlf-network)
- implement tests for MatriculationAPI
- generate coverage reports during tests
- generate pretty test reports
- provide pipelines with means to change chaincode branch/tag

# [v0.6.1](https://github.com/upb-uc4/hyperledger_api/compare/v0.6.0...v0.6.1) (2020-08-17)

## Refactor

- rename AbstractConnectionTrait to ConnectionTrait

# [v0.6.0](https://github.com/upb-uc4/hyperledger_api/compare/v0.5...v0.6.0) (2020-08-14)

## Feature

- Support new Matriculation Errors (DetailedError, GenericError)
- Incorporated Production Network (EnrollmentManager to provide enrollment functionality)

## Refactor

- Outsource Wallet-utilities
- Outsource Gateway-utilities
- Outsource + Parameterize ConnectionManager
- Exceptions Code Cleanup
- Traits Code Cleanup

## Usability

- Rework HyperledgerAPI (dedicated connections)
- Updated Pipelines to run multiple jobs in parallel
- begin changelog
- return Course errors by wrapping them as TransactionExceptions

# [v0.5.5](https://github.com/upb-uc4/hyperledger_api/compare/v0.5.4...v0.5.5) (2020-08-14)

## Usability

- Rework HyperledgerAPI.
	- no more disgusting ManagerHandling for outsiders
	- Access a single dedicated Connection (e.g. for course transactions) via 
	```new ConnectionCourses(<certificateName>, <channel>, <chaincode>, <wallet_path>, <network_config>)```
	- Invoke Transactions only via dedicated Methods. No more nasty transactionId-handling.
	- React to only TWO different Exceptions.
		- HyperledgerInnerExceptionTrait :: Something within the Framework went wrong
		- TransactionExceptionTrait :: Our Chaincode detected something wrong with your transaction. Maybe an item was malformed?

# [v0.5.4](https://github.com/upb-uc4/hyperledger_api/compare/v0.5.3...v0.5.4) (2020-08-11)

## Feature

- Incorporated Production Network
- EnrollmentManager to provide enrollment functionality
- first tests for matriculation errors

## Refactor

- Outsource Wallet-utilities and Gateway-utilities from ConnectionManager
- Parameterize ConnectionManager

## Bug Fixes

- error codes

## Usability

- Updated Pipelines to run multiple jobs in parallel

# [v0.5.3](https://github.com/upb-uc4/hyperledger_api/compare/v0.5.2...v0.5.3) (2020-08-11)

## Feature

- Update Exceptions according to API definition

# [v0.5.2](https://github.com/upb-uc4/hyperledger_api/compare/v0.5.1...v0.5.2) (2020-08-07)

## Bug Fix

- Adjust contractName from *.student to *.MatriculationData

# [v0.5.1](https://github.com/upb-uc4/hyperledger_api/compare/v0.5...v0.5.1) (2020-08-04)

## Feature

- Add Error Handling for Matriculation Errors (DetailedError, GenericError)

## Refactor

- Exceptions Code Cleanup
- Traits Code Cleanup

## Usability

- gitignore update 
- begin changelog

# [v0.5.0] (2020-08-04)

## Feature

- offer Access to Contracts
	- course
	- student
- Access tokens are available via resources
- Errors get wrapped for the courses

## Refactor

- update trait inheritance to provide easy management of additional contracts
