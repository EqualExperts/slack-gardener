resource "aws_s3_bucket" "ee_slack_gardener_lambdas" {
  bucket = "ee-slack-gardener-lambdas"
  acl    = "private"

  versioning {
    enabled = true
  }

}

resource "aws_s3_bucket_public_access_block" "lambdas_s3_public_access_block" {
  bucket = "${aws_s3_bucket.ee_slack_gardener_lambdas.id}"

  block_public_acls   = true
  block_public_policy = true
  ignore_public_acls = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "bucket-policy-lambdas" {
  bucket = "${aws_s3_bucket.ee_slack_gardener_lambdas.id}"
  policy =<<POLICY
{
    "Version": "2012-10-17",
    "Id": "Policy1507196867104",
    "Statement": [
        {
            "Sid": "Stmt1507196865806",
            "Effect": "Allow",
            "Principal": {"AWS":"044357138720"},
            "Action": "s3:*",
            "Resource": "arn:aws:s3:::ee-slack-gardener-lambdas"
        }
    ]
}
POLICY
}


resource "aws_s3_bucket" "ee_slack_gardener_logs" {
  bucket = "ee-slack-gardener-logs"
  acl    = "private"

  versioning {
    enabled = true
  }

}

resource "aws_s3_bucket_public_access_block" "logs_s3_public_access_block" {
  bucket = "${aws_s3_bucket.ee_slack_gardener_logs.id}"

  block_public_acls   = true
  block_public_policy = true
  ignore_public_acls = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_policy" "bucket-policy-logs" {
  bucket = "${aws_s3_bucket.ee_slack_gardener_logs.id}"
  policy =<<POLICY
{
    "Version": "2012-10-17",
    "Id": "Policy1507196867104",
    "Statement": [
        {
            "Sid": "Stmt1507196865806",
            "Effect": "Allow",
            "Principal": {"AWS":"044357138720"},
            "Action": "s3:*",
            "Resource": "arn:aws:s3:::ee-slack-gardener-logs"
        },
        {
          "Action": "s3:GetBucketAcl",
          "Effect": "Allow",
          "Resource": "arn:aws:s3:::ee-slack-gardener-logs",
          "Principal": { "Service": "logs.eu-west-1.amazonaws.com" }
        },
        {
          "Action": "s3:PutObject" ,
          "Effect": "Allow",
          "Resource": "arn:aws:s3:::ee-slack-gardener-logs/*",
          "Condition": { "StringEquals": { "s3:x-amz-acl": "bucket-owner-full-control" } },
          "Principal": { "Service": "logs.eu-west-1.amazonaws.com" }
        }
    ]
}
POLICY
}

