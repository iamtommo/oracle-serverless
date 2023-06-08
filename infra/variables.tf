variable "tenancy_ocid" {}
variable "user_ocid" {}
variable "fingerprint" {}
variable "private_key_path" {}

variable "app_name" {
  default = "tasker-server"
}

variable "region" {
  default = "uk-london-1"
}

variable "machine_shape" {
  default = "CI.Standard.E4.Flex"
  #default = "VM.Standard.E2.1.Micro"
}