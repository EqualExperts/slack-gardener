resource "aws_lambda_function" "ee_slack_gardener_lambda" {
  s3_bucket        = "${aws_s3_bucket.ee_slack_gardener_lambdas.id}"
  s3_key           = "ee-slack-gardener-lambda.jar"
  source_code_hash = "${data.aws_s3_bucket_object.jar_hash.body}"

  function_name    = "ee-slack-gardener"
  description      = "slack gardener lambda to clean up inactive ee slack channels"
  role             = "${aws_iam_role.ee_slack_gardener_iam_role.arn}"
  handler          = "com.equalexperts.slack.gardener.AwsLambda"
  runtime          = "java8"
  timeout          = 300
  memory_size = 512


  environment {
    variables = {
      SLACK_APIKEY  = "API_KEY"
      SLACK_BOT_APIKEY = "BOT_API_KEY"
      SLACK_URI    = "https://api.slack.com"
    }
  }

  tags {
    category = "ee"
  }
}

data "aws_s3_bucket_object" "jar_hash" {
  bucket = "${aws_s3_bucket.ee_slack_gardener_lambdas.id}"
  key = "ee-slack-gardener-lambda.jar.base64sha256"
}

