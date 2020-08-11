export MINIKUBE_IP="$(minikube ip)"
pushd src/test/resources
envsubst '${MINIKUBE_IP}' < connection_profile_kubernetes_template.yaml > connection_profile_kubernetes.yaml
popd

