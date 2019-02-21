# Configure security settings for Lambda
resource "aws_iam_role" "lambda_iam_role" {
  name = "LambdaRole"
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

# Attach role to Managed Policy
resource "aws_iam_policy_attachment" "lambda_basic_policy_attach" {
  name = "LambdaExecPolicy"
  roles = ["${aws_iam_role.lambda_iam_role.id}"]
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

resource "aws_iam_policy_attachment" "ssm_policy_attach" {
  name = "LambdaExecPolicy"
  roles = ["${aws_iam_role.lambda_iam_role.id}"]
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess"
}