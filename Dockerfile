FROM openjdk:11-jre-slim

WORKDIR	 /api

ARG JAR_FILE

COPY target/${JAR_FILE} /api/mercadoSolidarioApi.jar

EXPOSE 8080

CMD ["java", "-jar","mercadoSolidarioApi.jar"]