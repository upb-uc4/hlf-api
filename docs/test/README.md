# Scala Hyperledger API - Running Tests locally using the Production Network

## Prerequisites
0. Have a working UC4-chaincode-network running (Checkout the [hlf-network](https://github.com/upb-uc4/hlf-network)
    or alternatively fetch the latest network by executing
           ```sh
           ./fetch_prod_network.sh
           ```)

    - Have faketime installed
    ```sh
    sudo apt-get install faketime
    ```
    - Start the network by executing 
    ```sh
    ./restart.sh -t
    ```
   of the hlf-network.\
   Note:  use the testing flag to have the network create the testing identities.

1. Set up the Environment Variables: The necessary variables with their concrete values which need to be exported are provided at the end of the output of the network start script. They look similar to these:
    - find the Certificate Authority 
        ```shell script
        export UC4_KIND_NODE_IP=172.17.0.2
        ```
    - referencing the "connection_profile.yaml" 
    ```shell script
    export UC4_CONNECTION_PROFILE=/tmp/hyperledger/connection_profile_kubernetes_local.yaml
    ```
    - describing what kind of Network you are using
    ```shell script
    export UC4_TESTBASE_TARGET=PRODUCTION_NETWORK
    ```
    The given three export lines can simply be copied to the command line.

## Run the Tests :)

Run the tests from the hlf-api directory using:
    ```
    sbt clean compile testOnly "*ApprovalTests *CertificateErrorTests"
    ```

### Troubleshooting:

Without `sbt clean`, tests might fail without obvious reasons.
