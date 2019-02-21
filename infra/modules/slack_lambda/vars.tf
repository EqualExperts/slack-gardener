variable "region" {
  description = "The AWS region to deploy to (e.g. us-east-1)"
}

variable "account_number"{
  description = "Account ID in which to create the lamdba resources"
}

variable "lambda_logs_bucket_name" {
  description = "s3 bucket name for the lambda logs cloudwatch logs to be backed up there"
}

variable "lambda_artefact_bucket_name"{
  description = "s3 bucket name for the storage of the lambda artifacts"
}

variable "lambda_name"{
  description = "name of the lambda"
}

variable "lambda_display_name"{
  description = "display name of the lambda, for external communication purposes"
}

variable "lambda_description"{
  description = "description of the lambdas purpose"
}

variable "sns_topic" {
  description = "AWS SNS topic to send notifications to for failing lambdas"
}

variable "lambda_entrypoint" {
  description =" Entrypoint for lambda execution"
}