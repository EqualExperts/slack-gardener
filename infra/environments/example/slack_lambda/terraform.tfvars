terragrunt = {
  terraform {
    source = "../../../modules//slack_lambda"
  }

  include {
    path = "${find_in_parent_folders()}"
  }
}

lambda_artefact_bucket_name = "INSERT_BUCKET_ARTEFACT_NAME"
lambda_logs_bucket_name = "INSERT_LOG_BUCKET_NAME"
lambda_name = "INSERT_LAMBDA_NAME"
lambda_display_name = "INSERT_LAMBDA_DISPLAY_NAME"
lambda_description = "INSERT_LAMBDA_DESCRIPTION"
sns_topic = "INSERT_SNS_TOPIC"
lambda_entrypoint = "com.equalexperts.slack.lambda.AwsLambda"
