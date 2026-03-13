# EC2 instance configuration for running the Spring Boot backend application
# Uses t2.micro (free tier eligible) with Amazon Linux 2023

# Security group allowing inbound HTTP (8080) and SSH (22) traffic
resource "aws_security_group" "backend_sg" {
  name        = "survey-platform-backend-sg"
  description = "Security group for Survey Platform backend EC2 instance"
  vpc_id      = aws_vpc.main.id

  # Allow SSH access for deployment and maintenance
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "SSH access for deployment"
  }

  # Allow HTTP traffic on port 8080 for the Spring Boot application
  ingress {
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Backend API access"
  }

  # Allow all outbound traffic for package downloads and database connections
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }

  tags = {
    Name = "survey-platform-backend-sg"
  }
}

# Look up the latest Amazon Linux 2023 AMI for the EC2 instance
data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# EC2 instance running the backend application with Java 17
resource "aws_instance" "backend" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.backend_sg.id]
  key_name               = var.key_pair_name

  # User data script to install Java 17 and configure systemd service
  user_data = <<-EOF
              #!/bin/bash
              # Install Java 17 runtime for Spring Boot application
              yum update -y
              yum install -y java-17-amazon-corretto-headless

              # Create application directory
              mkdir -p /opt/survey-platform

              # Create systemd service for automatic startup and management
              cat > /etc/systemd/system/survey-platform.service << 'SERVICE'
              [Unit]
              Description=Survey Platform Backend API
              After=network.target

              [Service]
              Type=simple
              User=ec2-user
              ExecStart=/usr/bin/java -jar /opt/survey-platform/app.jar --spring.profiles.active=prod
              Restart=always
              RestartSec=10

              [Install]
              WantedBy=multi-user.target
              SERVICE

              # Enable the service to start on boot
              systemctl daemon-reload
              systemctl enable survey-platform
              EOF

  tags = {
    Name = "survey-platform-backend"
  }
}
