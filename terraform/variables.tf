# Input variables for the Survey Platform backend infrastructure
# These can be overridden via terraform.tfvars or environment variables

variable "aws_region" {
  description = "AWS region for deploying all infrastructure resources"
  type        = string
  default     = "eu-west-1"
}

variable "instance_type" {
  description = "EC2 instance type for the backend server (t2.micro is free tier)"
  type        = string
  default     = "t2.micro"
}

variable "key_pair_name" {
  description = "Name of the AWS EC2 key pair for SSH access to the backend instance"
  type        = string
  default     = "survey-platform-key"
}

variable "db_instance_class" {
  description = "RDS instance class for the PostgreSQL database (db.t3.micro is free tier)"
  type        = string
  default     = "db.t3.micro"
}

variable "db_username" {
  description = "Master username for the RDS PostgreSQL database"
  type        = string
  default     = "default"
  sensitive   = true
}

variable "db_password" {
  description = "Master password for the RDS PostgreSQL database"
  type        = string
  default     = "root"
  sensitive   = true
}
