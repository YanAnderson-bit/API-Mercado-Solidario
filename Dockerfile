FROM openjdk:11-jre-slim

WORKDIR	 /api

# Install Dockerize
ENV DOCKERIZE_VERSION v0.6.1
RUN apt-get update && apt-get install -y wget \
    && wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz


COPY target/API_Mercado_Solidario-latest.jar /api/mercadoSolidarioApi.jar

EXPOSE 8080

CMD ["dockerize", "-wait", "tcp://mysql:3306", "-timeout", "1m", "java", "-jar", "mercadoSolidarioApi.jar"]