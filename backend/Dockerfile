FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S boondi && adduser -S boondi -G boondi
COPY --from=builder /app/target/boondi-backend-*.jar app.jar
USER boondi
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
