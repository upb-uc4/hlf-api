pushd src/test/resources
envsubst '${NODE_IP}' < connection_profile_kubernetes_template.yaml > connection_profile_kubernetes.yaml
popd

echo "Applied cluster ip ${NODE_IP} to template."
