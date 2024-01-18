resource "aws_s3_bucket" "lambda_artefacts" {
  bucket = var.lambda_artefact_bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "lambda_artefacts_acl" {
    bucket = aws_s3_bucket.lambda_artefacts.id
    acl    = "private"
}

resource "aws_s3_bucket_versioning" "lambda_artefacts_versioning" {
    bucket = aws_s3_bucket.lambda_artefacts.id
    versioning_configuration {
        status = "Enabled"
    }
}

resource "aws_s3_bucket_public_access_block" "lambdas_s3_public_access_block" {
  bucket = aws_s3_bucket.lambda_artefacts.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "bucket-policy-lambdas" {
  bucket = aws_s3_bucket.lambda_artefacts.id
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Id": "Policy1507196867104",
    "Statement": [
        {
            "Sid": "Stmt1507196865806",
            "Effect": "Allow",
            "Principal": {"AWS":"${var.account_number}"},
            "Action": "s3:*",
            "Resource": "arn:aws:s3:::${var.lambda_artefact_bucket_name}"
        }
    ]
}
POLICY

}

resource "aws_s3_bucket" "lambda_logs" {
  bucket = var.lambda_logs_bucket_name

  lifecycle {
    prevent_destroy = true
  }
}

resource "aws_s3_bucket_acl" "lambda_logs_acl" {
    bucket = aws_s3_bucket.lambda_logs.id
    acl    = "private"
}

resource "aws_s3_bucket_versioning" "lambda_logs_versioning" {
    bucket = aws_s3_bucket.lambda_logs.id
    versioning_configuration {
        status = "Enabled"
    }
}

resource "aws_s3_bucket_public_access_block" "logs_s3_public_access_block" {
  bucket = aws_s3_bucket.lambda_logs.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "bucket-policy-logs" {
  bucket = aws_s3_bucket.lambda_logs.id
  policy = <<POLICY
{
    "Version": "2012-10-17",
    "Id": "Policy1507196867104",
    "Statement": [
        {
            "Sid": "Stmt1507196865806",
            "Effect": "Allow",
            "Principal": {"AWS":"${var.account_number}"},
            "Action": "s3:*",
            "Resource": "arn:aws:s3:::${var.lambda_logs_bucket_name}"
        },
        {
          "Action": "s3:GetBucketAcl",
          "Effect": "Allow",
          "Resource": "arn:aws:s3:::${var.lambda_logs_bucket_name}",
          "Principal": { "Service": "logs.eu-west-1.amazonaws.com" }
        },
        {
          "Action": "s3:PutObject" ,
          "Effect": "Allow",
          "Resource": "arn:aws:s3:::${var.lambda_logs_bucket_name}/*",
          "Condition": { "StringEquals": { "s3:x-amz-acl": "bucket-owner-full-control" } },
          "Principal": { "Service": "logs.eu-west-1.amazonaws.com" }
        }
    ]
}
POLICY

}

