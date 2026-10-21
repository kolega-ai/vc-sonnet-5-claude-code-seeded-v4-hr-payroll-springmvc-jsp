FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version
COPY src ./src
RUN ./gradlew bootWar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN mkdir -p /data/uploads
COPY --from=build /workspace/build/libs/hr-payroll-portal.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
