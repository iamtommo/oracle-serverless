output "region" {
  value = var.region
}

output "tenancy_namespace" {
  value = data.oci_objectstorage_namespace.tenancy_namespace.namespace
}

output compartment_id {
  value = oci_identity_compartment.tasker_compartment.id
}

output machine_shape {
  value = var.machine_shape
}

output availability_domain {
  value = data.oci_identity_availability_domains.ads.availability_domains[0].name
}

output subnet_id {
  value = oci_core_subnet.tasker_vcn_public_subnet.id
}

output "tasker_user_name" {
  value = oci_identity_user.tasker_user.name
}

output "tasker_user_email" {
  value = oci_identity_user.tasker_user.email
}

output "tasker_user_ocir_auth_token" {
  value = oci_identity_auth_token.tasker_user_auth_token.token
}

output "tasker_ocir_repo_name" {
  value = oci_artifacts_container_repository.tasker_ocir.display_name
}