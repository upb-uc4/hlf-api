# [v0.5.5 WIP](https://github.com/upb-uc4/hyperledger_api/compare/v0.5.4...develop) (2020-08-11)

## Feature

- 

## Bug Fixes

- 

## Refactor

- 

## Usability

- 

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

## Feature

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
