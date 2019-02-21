terragrunt = {
  terraform {
    source = "../../../modules//slack_lambda"
  }

  include {
    path = "${find_in_parent_folders()}"
  }
}

lambda_artefact_bucket_name = "ee-slack-gardener-lambdas"
lambda_logs_bucket_name = "ee-slack-gardener-logs"
lambda_name = "ee-slack-gardener"
lambda_display_name = "Slack Gardener"
lambda_description = "Slack Gardener Lambda warns and archives inactive slack channels"
sns_topic = "EE_Slack_Gardener_Engineers"
lambda_entrypoint = "com.equalexperts.slack.gardener.AwsLambda"