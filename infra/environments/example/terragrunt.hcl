remote_state {
    backend = "s3"

    config = {
        bucket         = "INSERT_BUCKET_STATE_NAME"
        key            = "${path_relative_to_include()}/terraform.tfstate"
        region         = "${get_env("AWS_DEFAULT_REGION", "eu-west-1")}"
        encrypt        = true
        dynamodb_table = "terraform-locks"
    }
}

generate "required_providers" {
    path      = "required_providers.tf"
    if_exists = "overwrite_terragrunt"
    contents  = <<EOF
terraform {
  required_providers {
    aws = {
        source = "hashicorp/aws"
        version = "4.67.0"
    }
  }
  required_version = "~> 1.4.6"
}
EOF
}
generate "provider" {
    path      = "aws_provider.tf"
    if_exists = "overwrite_terragrunt"
    contents  = <<EOF
provider "aws" {
  region = var.region
}
EOF
}

inputs = {
    account_number = "INSERT_ACCOUNT_NUMBER"
    region = "eu-west-1"
}
