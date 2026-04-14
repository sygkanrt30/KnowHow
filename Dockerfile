FROM eclipse-temurin:21-jdk-jammy AS builder
LABEL description="know_how"
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
LABEL description="know_how"

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError"

RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown appuser:appuser app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]