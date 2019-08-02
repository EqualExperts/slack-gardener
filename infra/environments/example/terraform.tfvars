terragrunt = {
  remote_state {
    backend = "s3"
    config {
      bucket         = "INSERT_BUCKET_STATE_NAME"
      key            = "${path_relative_to_include()}/terraform.tfstate"
      region         = "${get_env("AWS_DEFAULT_REGION", "eu-west-1")}"
      encrypt        = true
      dynamodb_table = "terraform-locks"
    }
  }

  terraform {
    extra_arguments "conditional_vars" {
      commands = [
        "apply",
        "destroy",
        "plan",
        "import",
        "push",
        "refresh"
      ]

      required_var_files = [
        "${get_parent_tfvars_dir()}/terraform.tfvars"
      ]

      optional_var_files = [
        "${get_parent_tfvars_dir()}/${path_relative_to_include()}/vars.tfvars"
      ]
    }
  }
}

account_number = "INSERT_ACCOUNT_NUMBER"
region = "eu-west-1"
