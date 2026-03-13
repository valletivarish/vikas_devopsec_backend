# S3 bucket configuration for storing backend deployment artifacts
# and static analysis reports from the CI/CD pipeline

resource "aws_s3_bucket" "artifacts" {
  bucket = "survey-platform-backend-artifacts-${var.aws_region}"

  tags = {
    Name = "survey-platform-backend-artifacts"
  }
}

# Enable versioning to keep history of deployed JARs
resource "aws_s3_bucket_versioning" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id
  versioning_configuration {
    status = "Enabled"
  }
}

# Block all public access to the artifacts bucket for security
resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket = aws_s3_bucket.artifacts.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
