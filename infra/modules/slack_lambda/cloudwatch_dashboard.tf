resource "aws_cloudwatch_dashboard" "lambda-dashboard" {
  dashboard_name = "${var.lambda_name}-dashboard"

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
                "query": "SOURCE '/aws/lambda/${var.lambda_name}' | fields @timestamp, @message\n| sort @timestamp desc",
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
                    [ "AWS/Lambda", "Invocations", "FunctionName", "${var.lambda_name}", { "period": 604800, "stat": "Sum", "label": "Lambda Invocations" } ],
                    [ ".", "Errors", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Errors" } ],
                    [ ".", "Duration", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Duration" } ],
                    [ ".", "Throttles", ".", ".", { "period": 604800, "stat": "Sum", "label": "Lambda Throttles" } ],
                    [ "AWS/Logs", "IncomingLogEvents", "LogGroupName", "/aws/lambda/${var.lambda_name}", { "period": 604800, "stat": "Sum", "label": "LogGroup IncomingLogEvents" } ],
                    [ ".", "IncomingBytes", ".", ".", { "period": 604800, "stat": "Sum", "label": "LogGroup IncomingBytes" } ],
                    [ "AWS/SNS", "PublishSize", "TopicName", "${var.sns_topic}", { "period": 604800, "stat": "Sum", "label": "SNS PublishSize" } ],
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