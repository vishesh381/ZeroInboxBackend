# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
# copy pom first to leverage Docker cache
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# now copy sources and build
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy the fat jar produced by Spring Boot
COPY --from=build /workspace/target/*.jar /app/app.jar

# Render injects PORT; we default to 8080 locally
ENV PORT=8080
EXPOSE 8080

# (Optional) tune JVM a bit
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

CMD ["java", "-jar", "/app/app.jar"]
