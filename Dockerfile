FROM maven:3.8.5-openjdk-17 AS build

ARG MVN_LIFECYCLE=install
ENV ENV_MVN_LIFECYCLE $MVN_LIFECYCLE

# Copy poms
COPY pom.xml .
COPY data-integration-service/pom.xml ./data-integration-service/
COPY data-integration-application/pom.xml ./data-integration-application/

# Copy src code
COPY data-integration-service/src ./data-integration-service/src
COPY data-integration-application/src ./data-integration-application/src

# Copy module
COPY data-integration-ui/ ./data-integration-ui/

# Copy extra files
COPY checkstyle.xml .
COPY lombok.config .

# Build
RUN mvn clean "$ENV_MVN_LIFECYCLE" -DskipTests

FROM maven:3.8.5-openjdk-17 AS data-integration-app
COPY --from=build data-integration-application/target/data-integration-application.jar .
EXPOSE 8001
ENTRYPOINT ["java", "-jar","/data-integration-application.jar"]
