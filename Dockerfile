# Use an official Maven image with JDK 11 as a base image
FROM maven:3.8.1-openjdk-11-slim as build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the application
RUN mvn clean package

# Runtime stage
FROM openjdk:11-jre-slim as runtime

# Set the working directory
WORKDIR /app

# Copy the built JAR file and the dependencies from the build stage
COPY --from=build /app/target/DockerMonitoring-1.0-SNAPSHOT.jar .

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "DockerMonitoring-1.0-SNAPSHOT.jar"]
