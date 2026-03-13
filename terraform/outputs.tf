# Output values for the Survey Platform backend infrastructure
# These values are needed for CI/CD pipeline configuration and frontend setup

output "backend_public_ip" {
  description = "Public IP address of the backend EC2 instance"
  value       = aws_instance.backend.public_ip
}

output "backend_public_dns" {
  description = "Public DNS hostname of the backend EC2 instance"
  value       = aws_instance.backend.public_dns
}

output "backend_url" {
  description = "Full URL for accessing the backend API"
  value       = "http://${aws_instance.backend.public_ip}:8080"
}

output "rds_endpoint" {
  description = "RDS PostgreSQL connection endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "rds_database_name" {
  description = "Name of the PostgreSQL database"
  value       = aws_db_instance.main.db_name
}

output "s3_artifacts_bucket" {
  description = "Name of the S3 bucket for deployment artifacts"
  value       = aws_s3_bucket.artifacts.bucket
}
