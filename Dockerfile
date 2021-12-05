FROM springci/graalvm-ce:java17-0.11.x as builder
WORKDIR /opt/graalvm
COPY *.kts gradlew /opt/graalvm/
COPY src /opt/graalvm/src
COPY gradle /opt/graalvm/gradle
COPY .gradle /opt/graalvm/.gradle

RUN ./gradlew installNativeImage
RUN ./gradlew nativeImage

FROM openjdk:17-slim-buster
COPY --from=builder /opt/graalvm/build/executable/api-scraper /bin/api-scraper
CMD ["sh", "-c", "/bin/api-scraper /etc/config.toml"]
