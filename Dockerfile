# We use a Maven image to compile the Java code into a runnable .jar file
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compile the code and skip tests to speed up the build
RUN mvn clean package -DskipTests

# STAGE 2: Run the Application
# We switch to a tiny, lightweight Java image to actually run the app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the compiled .jar file from STAGE 1 into this final container
COPY --from=build /app/target/*.jar app.jar

# Expose the port our Spring Boot web server runs on
EXPOSE 8088

# The command to start the application
ENTRYPOINT ["java", "-jar", "app.jar"]