FROM eclipse-temurin:26-jdk AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon

RUN JAR_FILE=$(find build/libs -name "*.jar" ! -name "*-plain.jar" -print -quit) \
    && cp "$JAR_FILE" app.jar


FROM eclipse-temurin:26-jdk AS runner

WORKDIR /app

ENV APP_ENVIRONMENT=dev
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx768m"

RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=builder --chown=spring:spring /app/app.jar ./app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]