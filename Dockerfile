FROM ubuntu:24.04 AS java-base

ARG TARGETARCH
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN set -eu; \
    case "${TARGETARCH}" in \
        amd64) java_arch=x64 ;; \
        arm64) java_arch=aarch64 ;; \
        *) echo "Unsupported JDK architecture: ${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl; \
    rm -rf /var/lib/apt/lists/*; \
    archive_url="https://download.oracle.com/java/25/latest/jdk-25_linux-${java_arch}_bin.tar.gz"; \
    curl -fsSL "${archive_url}" -o /tmp/jdk.tar.gz; \
    checksum="$(curl -fsSL "${archive_url}.sha256")"; \
    printf '%s  %s\n' "${checksum}" /tmp/jdk.tar.gz | sha256sum -c -; \
    mkdir -p "${JAVA_HOME}"; \
    tar -xzf /tmp/jdk.tar.gz -C "${JAVA_HOME}" --strip-components=1; \
    rm /tmp/jdk.tar.gz; \
    java_version="$(sed -n 's/^JAVA_VERSION="\(.*\)"$/\1/p' "${JAVA_HOME}/release")"; \
    case "${java_version}" in \
        25.*) ;; \
        *) echo "Expected Oracle JDK 25, found ${java_version}" >&2; exit 1 ;; \
    esac; \
    if [ "$(printf '%s\n' 25.0.5.1 "${java_version}" | sort -V | head -n 1)" != 25.0.5.1 ]; then \
        echo "JDK ${java_version} predates the fix for CVE-2026-114938" >&2; \
        exit 1; \
    fi

FROM java-base AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon

RUN JAR_FILE=$(find build/libs -name "*.jar" ! -name "*-plain.jar" -print -quit) \
    && cp "$JAR_FILE" app.jar


FROM java-base AS runner

WORKDIR /app

ENV APP_ENVIRONMENT=dev
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx768m"

RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=builder --chown=spring:spring /app/app.jar ./app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
