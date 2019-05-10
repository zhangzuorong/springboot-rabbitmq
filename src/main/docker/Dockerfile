FROM java:8
VOLUME /tmp
ADD springboot-rabbitmq-1.0-SNAPSHOT.jar app.jar
ENV TZ=Asia/Shanghai
RUN bash -c 'touch /app.jar' \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8667