# Stage 1: Build Spring Boot application
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment with multiple language compilers
FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install system dependencies and compilers
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y \
    software-properties-common \
    wget \
    curl \
    build-essential \
    gcc \
    g++ \
    openjdk-8-jdk \
    openjdk-11-jdk \
    openjdk-17-jdk \
    openjdk-21-jdk \
    python3 \
    python3-pip \
    python3-dev \
    ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Add deadsnakes PPA for Python 3.8
RUN add-apt-repository ppa:deadsnakes/ppa -y && \
    apt-get update && \
    apt-get install -y \
    python3.8 \
    python3.8-dev \
    python3.8-distutils

# Create symbolic link for Python 3.8
RUN ln -sf /usr/bin/python3.8 /usr/local/bin/python3.8 && \
    ln -sf /usr/local/bin/python3.8 /usr/bin/python3

# Copy the built Spring Boot JAR from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the application port
EXPOSE 8080

# Set the working directory
WORKDIR /app

# Default command to run the Spring Boot application
CMD ["java", "-jar", "/app/app.jar"]