FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system demo && adduser --system --ingroup demo demo

ENV LOG_PATH=/var/log/app
ENV SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/taskdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
ENV SPRING_DATA_REDIS_HOST=redis
ENV ROCKETMQ_NAME_SERVER=rocketmq-namesrv:9876

RUN mkdir -p /var/log/app && chown -R demo:demo /var/log/app

COPY --from=build /workspace/target/*.jar app.jar

USER demo:demo

EXPOSE 8080

VOLUME ["/var/log/app"]

ENTRYPOINT ["sh", "-c", "java -Drocketmq.log.root=/var/log/app/rocketmqlogs $JAVA_OPTS -jar /app/app.jar"]
