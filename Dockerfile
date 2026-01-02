FROM eclipse-temurin:21-jdk AS build

RUN apt-get update && apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" \
      > /etc/apt/sources.list.d/sbt.list && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" \
      > /etc/apt/sources.list.d/sbt_old.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" \
      -o /etc/apt/trusted.gpg.d/sbt.asc && \
    apt-get update && apt-get install -y sbt && \
    apt-get clean

WORKDIR /app

COPY chat-server ./chat-server
WORKDIR /app/chat-server
RUN sbt assembly

WORKDIR /app
COPY chat-client ./chat-client
WORKDIR /app/chat-client
RUN sbt assembly

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/chat-server/target/scala-*/chat-server-assembly-*.jar chat-server.jar
COPY --from=build /app/chat-client/target/scala-*/chat-client-assembly-*.jar chat-client.jar

COPY run-server.sh .
COPY run-client.sh .

RUN chmod +x run-server.sh run-client.sh

ENTRYPOINT ["bash"]
