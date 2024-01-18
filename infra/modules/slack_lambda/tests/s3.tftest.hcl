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

run "verify_s3_private" {
    command = plan # Test runs on Terraform apply, you can use `plan` if you want

    assert {
        condition     = aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.block_public_acls == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.block_public_acls}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.block_public_policy == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.block_public_policy}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.ignore_public_acls == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.ignore_public_acls}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.restrict_public_buckets == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.lambdas_s3_public_access_block.restrict_public_buckets}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.logs_s3_public_access_block.block_public_acls == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.logs_s3_public_access_block.block_public_acls}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.logs_s3_public_access_block.block_public_policy == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.logs_s3_public_access_block.block_public_policy}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.logs_s3_public_access_block.ignore_public_acls == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.logs_s3_public_access_block.ignore_public_acls}"
    }

    assert {
        condition     = aws_s3_bucket_public_access_block.logs_s3_public_access_block.restrict_public_buckets == true
        error_message = "Expected: true Actual: ${aws_s3_bucket_public_access_block.logs_s3_public_access_block.restrict_public_buckets}"
    }
}
