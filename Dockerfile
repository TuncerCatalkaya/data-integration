FROM maven:3.8.5-openjdk-17 AS build

ARG MVN_LIFECYCLE=install
ENV ENV_MVN_LIFECYCLE $MVN_LIFECYCLE

# Copy poms
COPY pom.xml .
COPY data-integration-model/pom.xml ./data-integration-model/
COPY data-integration-api/pom.xml ./data-integration-api/
COPY data-integration-service/pom.xml ./data-integration-service/
COPY data-integration-application/pom.xml ./data-integration-application/
COPY data-integration-documentation/pom.xml ./data-integration-documentation/

# Copy src code
COPY data-integration-model/src ./data-integration-model/src
COPY data-integration-api/src ./data-integration-api/src
COPY data-integration-service/src ./data-integration-service/src
COPY data-integration-application/src ./data-integration-application/src
COPY data-integration-documentation/src ./data-integration-documentation/src

# Copy frontend
COPY data-integration-ui/ ./data-integration-ui/

# Copy extra files
COPY checkstyle.xml .
COPY lombok.config .

# Build
RUN mvn clean "$ENV_MVN_LIFECYCLE" -DskipTests

FROM maven:3.8.5-openjdk-17 AS data-integration-app
COPY --from=build data-integration-application/target/data-integration-application.jar .
COPY --from=build data-integration-ui/.env ./data-integration-ui/.env
EXPOSE 8001
ENTRYPOINT ["java", "-jar", "/data-integration-application.jar"]
