terragrunt = {
  terraform {
    source = "../../../modules//slack_lambda"
  }

  include {
    path = "${find_in_parent_folders()}"
  }
}

