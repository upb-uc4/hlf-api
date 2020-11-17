# Scala Hyperledger API - Running Tests

## Running test locally using the production network

1. Checkout the [hlf-network](https://github.com/upb-uc4/hlf-network).
2. Execute `./restart -t` of the hlf-network.
3. Export the environment variables from the end of the output. They look similar to
    ```
    export UC4_KIND_NODE_IP=172.17.0.2
    export UC4_CONNECTION_PROFILE=/tmp/hyperledger/connection_profile_kubernetes_local.yaml
    export UC4_TESTBASE_TARGET=PRODUCTION_NETWORK
    ```
4. Run test from the hlf-api directory using:
    ```
    sbt testOnly "*ApprovalTests *CertificateErrorTests"
    ```
5. Troubleshooting:
    - If tests fail, try running 
      ```
      sbt clean
      ```
      before testing.

## Prerequisites
0. Have a working UC4-chaincode-network running 
    (download from [hlf-network](https://github.com/upb-uc4/hlf-network)
    and  [chaincode](https://github.com/upb-uc4/hlf-chaincode)
    or use the fetch-scripts)
    
    - fetch the latest network
    ```sh
    ./fetch_prod_network.sh
    ```

    - install faketime
    ```sh
    sudo apt-get install faketime
    ```

    - prepare folders for kind
    ```sh
    sudo mkdir -p /data/development/hyperledger
    sudo chmod -R 777 /data/development
    ```

    - start the network
    Note:  use the testing flag to have the network create the testing identities.
    ```sh
    ./restart.sh -t
    ```

1. Set up the Environment Variables
    - referencing the "connection_profile.yaml" 
    ```shell script
    export UC4_CONNECTION_PROFILE ='<hlf-network-path(e.g. ./hlf-network)>/assets/connection_profile_kubernetes_local.yaml'
    ```
    - describing what kind of Network you are using
    ```shell script
    export UC4_TESTBASE_TARGET='PRODUCTION_NETWORK'
    ```
    - find the Certificate Authority 
    ```shell script
    export UC4_KIND_NODE_IP=$(source <hlf-network-path(e.g. ./hlf-network)>/scripts/util.sh && get_worker_ip)
    ```

## Run the Tests :)

```sbt
sbt testOnly "*ApprovalTests *CertificateErrorTests"
```
