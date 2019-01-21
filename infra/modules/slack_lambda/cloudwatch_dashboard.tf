resource "aws_cloudwatch_dashboard" "ee-slack-gardener-dashboard" {
  dashboard_name = "ee-slack-gardener-dashboard"

  dashboard_body = <<EOF
 {
    "widgets": [
        {
            "type": "log",
            "x": 0,
            "y": 6,
            "width": 24,
            "height": 6,
            "properties": {
                "query": "SOURCE '/aws/lambda/ee-slack-gardener' | fields @timestamp, @message\n| sort @timestamp desc\n| limit 1000",
                "region": "eu-west-1"
            }
        },
        {
            "type": "metric",
            "x": 0,
            "y": 0,
            "width": 24,
            "height": 6,
            "properties": {
                "metrics": [
                    [ "AWS/Lambda", "Invocations", "FunctionName", "ee-slack-gardener", { "period": 604800, "stat": "Sum", "label": "Lambda Invocations" } ],
                    [ ".", "Errors", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Errors" } ],
                    [ ".", "Duration", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Duration" } ],
                    [ ".", "Throttles", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Throttles" } ],
                    [ "AWS/Logs", "IncomingLogEvents", "LogGroupName", "/aws/lambda/ee-slack-gardener", { "period": 604800, "stat": "Sum", "label": "LogGroup IncomingLogEvents" } ],
                    [ ".", "IncomingBytes", ".", ".", { "period": 604800, "stat": "Sum", "label": "LogGroup IncomingBytes" } ],
                    [ "AWS/SNS", "PublishSize", "TopicName", "EE_Slack_Gardener_Engineers", { "period": 604800, "stat": "Sum", "label": "SNS PublishSize" } ],
                    [ ".", "NumberOfNotificationsFailed", ".", ".", { "period": 604800, "stat": "Sum", "label": "SNS NotificationsFailed" } ],
                    [ ".", "NumberOfMessagesPublished", ".", ".", { "period": 604800, "stat": "Sum", "label": "SNS NumberOfMessagesPublished" } ],
                    [ ".", "NumberOfNotificationsDelivered", ".", ".", { "period": 604800, "stat": "Sum", "label": "SNS NotificationsDelivered" } ]
                ],
                "view": "singleValue",
                "region": "eu-west-1",
                "title": "Sum of Metrics",
                "period": 300
            }
        }
    ]
}
 EOF
}