# RDS PostgreSQL configuration for the Survey Platform database
# Uses db.t3.micro (free tier eligible) in a private subnet for security

# Subnet group placing the database in private subnets (no public access)
resource "aws_db_subnet_group" "main" {
  name       = "survey-platform-db-subnet-group"
  subnet_ids = [aws_subnet.private_a.id, aws_subnet.private_b.id]

  tags = {
    Name = "survey-platform-db-subnet-group"
  }
}

# Security group allowing PostgreSQL traffic only from the backend EC2 instance
resource "aws_security_group" "db_sg" {
  name        = "survey-platform-db-sg"
  description = "Security group for Survey Platform RDS PostgreSQL instance"
  vpc_id      = aws_vpc.main.id

  # Allow PostgreSQL connections from the backend security group only
  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.backend_sg.id]
    description     = "PostgreSQL access from backend EC2"
  }

  tags = {
    Name = "survey-platform-db-sg"
  }
}

# RDS PostgreSQL instance for storing survey data
resource "aws_db_instance" "main" {
  identifier             = "survey-platform-db"
  engine                 = "postgres"
  engine_version         = "15.4"
  instance_class         = var.db_instance_class
  allocated_storage      = 20
  storage_type           = "gp3"
  db_name                = "surveyplatform"
  username               = var.db_username
  password               = var.db_password
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db_sg.id]
  skip_final_snapshot    = true
  publicly_accessible    = false

  tags = {
    Name = "survey-platform-db"
  }
}
