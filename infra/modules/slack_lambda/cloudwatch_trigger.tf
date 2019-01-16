resource "aws_cloudwatch_event_rule" "every_weekday_at_nine_am" {
    name = "mon-fri-0900Z"
    description = "Daily monday-friday at 9am, UTC"
    schedule_expression = "cron(0 9 ? * MON-FRI *)"
}

resource "aws_cloudwatch_event_target" "call_slack_gardener_every_weekday_at_nine_am" {
    rule = "${aws_cloudwatch_event_rule.every_weekday_at_nine_am.name}"
    target_id = "1db6a5b2-48c7-4d3f-a2e5-34c1f7f862b7"
    arn = "${aws_lambda_function.ee_slack_gardener_lambda.arn}"
}

resource "aws_lambda_permission" "allow_cloudwatch_to_call_check_foo" {
    statement_id = "AllowExecutionFromCloudWatch"
    action = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.ee_slack_gardener_lambda.function_name}"
    principal = "events.amazonaws.com"
    source_arn = "${aws_cloudwatch_event_rule.every_weekday_at_nine_am.arn}"
}