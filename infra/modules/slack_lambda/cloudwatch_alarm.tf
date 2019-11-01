resource "aws_cloudwatch_log_group" "lambda_cw_log_group" {
  name = "/aws/lambda/${var.lambda_name}"
}

resource "aws_cloudwatch_metric_alarm" "lambda_error_alarm" {
  alarm_name          = var.lambda_display_name
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "1"
  metric_name         = "Errors"
  namespace           = "AWS/Lambda"
  period              = "86400"
  statistic           = "Minimum"
  datapoints_to_alarm = 1
  threshold           = "0"
  alarm_description   = "The ${var.lambda_name} lambda has failed to run in the last 24 hours"
  alarm_actions = [
    aws_sns_topic.lambda_error_alarm.arn,
  ]

  dimensions = {
    FunctionName = var.lambda_name
  }
}

resource "aws_sns_topic" "lambda_error_alarm" {
  name   = var.sns_topic
  policy = <<EOF
    {
  "Version": "2008-10-17",
  "Id": "__default_policy_ID",
  "Statement": [
    {
      "Sid": "__default_statement_ID",
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": [
        "SNS:GetTopicAttributes",
        "SNS:SetTopicAttributes",
        "SNS:AddPermission",
        "SNS:RemovePermission",
        "SNS:DeleteTopic",
        "SNS:Subscribe",
        "SNS:ListSubscriptionsByTopic",
        "SNS:Publish",
        "SNS:Receive"
      ],
      "Resource": "arn:aws:sns:eu-west-1:${var.account_number}:${var.sns_topic}",
      "Condition": {
        "StringEquals": {
          "AWS:SourceOwner": "${var.account_number}"
        }
      }
    }
  ]
}
EOF

}

