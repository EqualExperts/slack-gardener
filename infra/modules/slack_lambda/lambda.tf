resource "aws_lambda_function" "lambda" {
  s3_bucket        = aws_s3_bucket.lambda_artefacts.id
  s3_key           = "${var.lambda_name}-lambda.jar"
  source_code_hash = data.aws_s3_bucket_object.jar_hash.body
  publish          = true

  function_name = var.lambda_name
  description   = var.lambda_description
  role          = aws_iam_role.lambda_iam_role.arn
  handler       = var.lambda_entrypoint
  runtime       = "java8"
  timeout       = 300
  memory_size   = 512
}

data "aws_s3_bucket_object" "jar_hash" {
  bucket = aws_s3_bucket.lambda_artefacts.id
  key    = "${var.lambda_name}-lambda.jar.base64sha256"
}

