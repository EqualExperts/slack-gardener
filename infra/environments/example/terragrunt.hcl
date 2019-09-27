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

terraform  {
    extra_arguments "conditional_vars" {
        commands = [
            "apply",
            "destroy",
            "plan",
            "import",
            "push",
            "refresh",
        ]

        ## The order of these files is reverse order of variable preference.
        ## (eg. Variables in earlier files will be overriden by variables with the same name in later files)
        ## This allows us to ensure that variables in 'child' .hcl files override the variables in 'parent' files
        optional_var_files = [
            "${get_parent_terragrunt_dir()}/terraform.tfvars",
            "${get_terragrunt_dir()}/terraform.tfvars"
        ]
    }

}


inputs = {
    account_number = "INSERT_ACCOUNT_NUMBER"
    region = "eu-west-1"
}
