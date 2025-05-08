FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/bloggingservice-0.0.1-SNAPSHOT.jar bloggingservice.jar

ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENV AWS_REGION=us-east-2

EXPOSE 8085

CMD ["java","-jar","bloggingservice.jar"]
