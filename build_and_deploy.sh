#!/bin/sh
set -e # exit on error
#set -x # print expanded commands

echo "Building server..."
mvn clean install

echo "Building docker image..."
docker buildx build --platform linux/amd64 -t tasker .

OCI_COMPARTMENT_ID=$(terraform -chdir=infra output -raw compartment_id)
OCI_MACHINE_SHAPE=$(terraform -chdir=infra output -raw machine_shape)
OCI_AVAILABILITY_DOMAIN=$(terraform -chdir=infra output -raw availability_domain)
OCI_SUBNET_ID=$(terraform -chdir=infra output -raw subnet_id)
OCIR_REGION=$(terraform -chdir=infra output -raw region)
OCIR_TENANCY_NAMESPACE=$(terraform -chdir=infra output -raw tenancy_namespace)
OCIR_USER=$(terraform -chdir=infra output -raw tasker_user_name)
OCIR_REPO_NAME=$(terraform -chdir=infra output -raw tasker_ocir_repo_name)
OCIR_USER_NAME=$(terraform -chdir=infra output -raw tasker_user_name)
OCIR_USER_TOKEN=$(terraform -chdir=infra output -raw tasker_user_ocir_auth_token)

IMAGE_URL=${OCIR_REGION}.ocir.io/${OCIR_TENANCY_NAMESPACE}/${OCIR_REPO_NAME}/tasker:latest

docker tag tasker $IMAGE_URL

echo "Pushing docker image to ${IMAGE_URL}"
echo ${OCIR_USER_TOKEN} | docker login ${OCIR_REGION}.ocir.io \
    -u ${OCIR_TENANCY_NAMESPACE}/${OCIR_USER_NAME} --password-stdin
docker push $IMAGE_URL

echo "Creating container instance..."
create_container=$(oci container-instances container-instance create \
    --display-name tasker-server \
    --compartment-id $OCI_COMPARTMENT_ID \
    --shape $OCI_MACHINE_SHAPE \
    --shape-config '{"ocpus": 1, "memory-in-gbs": 1}' \
    --availability-domain $OCI_AVAILABILITY_DOMAIN \
    --vnics '[{"subnet-id": "'$OCI_SUBNET_ID'", "is-public-ip-assigned": true}]' \
    --containers '[{"display-name": "tasker-server","image-url": "'${IMAGE_URL}'"}]')

container_instance_id=$(echo $create_container | jq -r .data.id)
echo "Created container id: ${container_instance_id}"

# wait for container to come up
while true; do
    echo "Checking container liveness..."
    instance_info=$(oci container-instances container-instance get --container-instance-id $container_instance_id)
    lifecycle_state=$(echo "$instance_info" | jq -r '.data."lifecycle-state"')
    echo "Status: ${lifecycle_state}"

    if [ "$lifecycle_state" == "ACTIVE" ]; then
        break
    fi

    sleep 10
done

# print public ip address
vnic_id=$(oci container-instances container-instance get --container-instance-id $container_instance_id | jq -r '.data.vnics[0]."vnic-id"')
public_ip=$(oci network vnic get --vnic-id $vnic_id | jq -r '.data."public-ip"')
echo "Server available at: http://${public_ip}:8080"