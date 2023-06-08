terraform {
    required_providers {
        oci = {
            source  = "hashicorp/oci"
            version = "~> 4.0"
        }
    }
}

provider "oci" {
    tenancy_ocid = var.tenancy_ocid
    user_ocid = var.user_ocid
    private_key_path = var.private_key_path
    fingerprint = var.fingerprint
    region = var.region
}

data "oci_objectstorage_namespace" "tenancy_namespace" {}

resource "oci_identity_compartment" "tasker_compartment" {
    name = "tasker_compartment"
    compartment_id = var.tenancy_ocid
    description = "tasker compartment"
}

data "oci_identity_availability_domains" "ads" {
    compartment_id = oci_identity_compartment.tasker_compartment.id
}

module "vcn" {
    source  = "oracle-terraform-modules/vcn/oci"
    version = "3.1.0"

    compartment_id = oci_identity_compartment.tasker_compartment.id
    region = var.region

    vcn_name = "tasker_vcn"
    vcn_dns_label = "taskervcn"
    vcn_cidrs = ["10.0.0.0/16"]

    create_internet_gateway = true
    create_nat_gateway = true
    create_service_gateway = true
}

resource "oci_core_security_list" "tasker_public_security_list" {
    compartment_id = oci_identity_compartment.tasker_compartment.id
    vcn_id = module.vcn.vcn_id

    display_name = "security-list-for-public-subnet"

    egress_security_rules {
        stateless        = false
        destination      = "0.0.0.0/0"
        destination_type = "CIDR_BLOCK"
        protocol         = "all"
    }
    ingress_security_rules {
        stateless   = false
        source      = "0.0.0.0/0"
        source_type = "CIDR_BLOCK"
        protocol    = "6"
        tcp_options {
            min = 22
            max = 22
        }
    }
    ingress_security_rules {
        stateless   = false
        source      = "0.0.0.0/0"
        source_type = "CIDR_BLOCK"
        protocol    = "6"  # TCP
        tcp_options {
            min = 8080
            max = 8080
        }
    }
    ingress_security_rules {
        stateless   = false
        source      = "0.0.0.0/0"
        source_type = "CIDR_BLOCK"
        protocol    = "1"

        icmp_options {
            type = 3
            code = 4
        }
    }
    ingress_security_rules {
        stateless   = false
        source      = "10.0.0.0/16"
        source_type = "CIDR_BLOCK"
        protocol    = "1"

        icmp_options {
            type = 3
        }
    }
}

resource "oci_core_subnet" "tasker_vcn_public_subnet" {
    compartment_id = oci_identity_compartment.tasker_compartment.id
    vcn_id = module.vcn.vcn_id
    cidr_block = "10.0.0.0/24"

    route_table_id = module.vcn.ig_route_id
    security_list_ids = [oci_core_security_list.tasker_public_security_list.id]
    display_name = "public-subnet"
}

resource "oci_artifacts_container_repository" "tasker_ocir" {
    compartment_id = oci_identity_compartment.tasker_compartment.id
    display_name = "taskerimages"

    is_public = false
    readme {
        content = "tasker image repo"
        format = "text/plain"
    }
}

resource "oci_identity_auth_token" "tasker_user_auth_token" {
    user_id = oci_identity_user.tasker_user.id
    description = "tasker user auth token"
}

resource "oci_identity_group" "tasker_user_group" {
    compartment_id = var.tenancy_ocid
    name = "tasker_users"
    description = "tasker user group"
}

resource "oci_identity_policy" "tasker_policy" {
    depends_on = [oci_identity_group.tasker_user_group]
    compartment_id = var.tenancy_ocid
    description = "tasker policy"
    name = "tasker_policy"
    statements = [
        # ocir read
        "Allow group ${oci_identity_group.tasker_user_group.name} to manage repos in tenancy"
    ]
}

resource "oci_identity_user" "tasker_user" {
    compartment_id = var.tenancy_ocid
    description = "tasker user"
    name = "tasker"
    email = "tasker@oracle.com"
}

resource "oci_identity_user_group_membership" "tasker_user_group_membership" {
    group_id = oci_identity_group.tasker_user_group.id
    user_id = oci_identity_user.tasker_user.id
}

# Dynamic group & policy to let container instance access image repo & logs
resource "oci_identity_dynamic_group" "oci_ci_dyngroup" {
    compartment_id = var.tenancy_ocid
    description = "ci dyngroup"
    name = "tasker_container_instance_dyngroup"
    matching_rule = "ALL {resource.type='computecontainerinstance'}"
}
resource "oci_identity_policy" "oci_ci_policy" {
    depends_on = [oci_identity_dynamic_group.oci_ci_dyngroup]
    compartment_id = var.tenancy_ocid
    description = "ocir policy"
    name = "tasker_container_instance_dyngroup_policy"
    statements = ["Allow dynamic-group ${oci_identity_dynamic_group.oci_ci_dyngroup.name} to manage repos in tenancy"]
}