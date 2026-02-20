# Stage 1: Build the application
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy the certificate and import it to the truststore
COPY thehutgroup.crt .
RUN keytool -import -alias thehutgroup -keystore $JAVA_HOME/lib/security/cacerts -file thehutgroup.crt -storepass changeit -noprompt

# Copy gradle files and project source
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY src/ src/

# Build the application
RUN ./gradlew build -x test

# Stage 2: Run the application
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
