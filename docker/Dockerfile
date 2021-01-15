FROM openjdk:16-jdk-alpine3.12

ADD https://github.com/ufoscout/docker-compose-wait/releases/latest/download/wait /wait
RUN chmod +x /wait

WORKDIR /safepass
RUN wget https://github.com/Kamilkime/SafePass/releases/latest/download/SafePass.jar

CMD /wait && java -jar /safepass/SafePass.jar
