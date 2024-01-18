# tests/main.tftest.hcl

variables {
    bucket_prefix = "test"
    region = "eu-west-1"
    account_number = "123456789"
    lambda_logs_bucket_name = "lambda_logs_bucket_test_name"
    lambda_artefact_bucket_name = "lambda_artefact_bucket_test_name"
    lambda_name = "test_lambda"
    lambda_display_name = "Test Lambda"
    lambda_description = "This is a test lambda"
    sns_topic = "TEST_SNS_TOPIC"
    lambda_entrypoint = "com.equalexperts.slack.aws.lambda.Entrypoint"
}

run "verify_lambda_iam_policies_present" {
    command = plan # Test runs on Terraform apply, you can use `plan` if you want

    assert { # uses managed basic lambda execution role
        condition     = aws_iam_policy_attachment.lambda_basic_policy_attach.policy_arn == "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
        error_message = "Expected: arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole Actual: ${aws_iam_policy_attachment.lambda_basic_policy_attach.policy_arn}"
    }

    assert { # can also access ssm
        condition     = aws_iam_policy_attachment.ssm_policy_attach.policy_arn == "arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess"
        error_message = "Expected: arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess Actual: ${aws_iam_policy_attachment.ssm_policy_attach.policy_arn}"
    }

}
