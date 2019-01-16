resource "aws_cloudwatch_log_group" "ee_slack_gardener_cw_log_group" {
  name = "/aws/lambda/ee-slack-gardener"
}


resource "aws_cloudwatch_metric_alarm" "slack_gardener_error" {
  alarm_name = "Slack Gardener"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods = "1"
  metric_name = "Error"
  namespace = "AWS/Lambda"
  period = "86400"
  statistic = "Minimum"
  threshold = "0"
  alarm_description = "The slack gardener lambda has failed to run in the last 24 hours"
  alarm_actions = [
    "${aws_sns_topic.gardener_error_alarm.arn}"]

  dimensions {
    FunctionName = "slack-gardener"
  }
}

resource "aws_sns_topic" "gardener_error_alarm" {
  name = "EE_Slack_Gardener_Engineers"
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
      "Resource": "arn:aws:sns:eu-west-1:044357138720:EE_Slack_Gardener_Engineers",
      "Condition": {
        "StringEquals": {
          "AWS:SourceOwner": "044357138720"
        }
      }
    }
  ]
}
EOF
}

