resource "aws_cloudwatch_log_group" "ee_slack_gardener_cw_log_group" {
  name = "/aws/lambda/ee-slack-gardener"
}

resource "aws_cloudwatch_metric_alarm" "slack_gardener_error" {
  alarm_name                = "Slack Gardener"
  comparison_operator       = "GreaterThanThreshold"
  evaluation_periods        = "1"
  metric_name               = "Error"
  namespace                 = "AWS/Lambda"
  period                    = "86400"
  statistic                 = "Minimum"
  threshold                 = "0"
  alarm_description         = "The slack gardener lambda has failed to run in the last 24 hours"


  dimensions {
    FunctionName = "slack-gardener"
  }
}
