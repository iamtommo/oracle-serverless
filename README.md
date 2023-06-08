# Oracle serverless
Dropwizard server with automatic OCI provisioning for 1-command deployment to ContainerInstances

API:
```
    GET     /task
    POST    /task
    DELETE  /task/{taskId}
    GET     /task/{taskId}
    PUT     /task/{taskId}
```

Where task is some form of:
```json
{
  "id": "<long>",
  "description": "<string>",
  "date": "<yyyy-mm-dd>",
  "completed": "<bool>"
}
```

## Dependencies

- `mvn`
- `oci cli`
- `jq`
- `docker`


## Cloud infrastructure (IaC)
Terraform provisioning of the basic infra needed to run Container Instances (ocir image repo, compartment, policies, users, etc)

Required terraform vars:
- `tenancy_ocid`
- `user_ocid`
- `private_key_path`
- `fingerprint`

Provisioning:
```sh
terraform apply -var-file="secrets.tfvars"
```

## Build & run locally

1. `mvn clean install`
2. `java -jar target/tasker-1.0.jar server config.yml`

## Build & run locally with Docker
1. `mvn clean install`
2. `docker build -t tasker .`
3. `docker run -o 8080:8080 tasker`

## Build & deploy to Oracle Cloud
1. `./build_and_deploy.sh`
Server should be up and running when greeted with the following output:
```
Checking container liveness...
Status: ACTIVE
Server available at: http://<address>:8080
```

## Logging
In production config, logs are written in json format to `/tasker/logs/server.log`.

## Storage
No persistent storage. Simply an in-memory threadsafe store.