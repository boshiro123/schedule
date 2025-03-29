FROM openjdk:21
WORKDIR /app
ARG JAR_FILE=schedule-0.0.1-SNAPSHOT.jar
COPY target/${JAR_FILE} /app/${JAR_FILE}

# # Создаем директорию для изображений и устанавливаем права
# RUN mkdir -p /app/images && \
#     chmod 777 /app/images

CMD ["java", "-jar", "schedule-0.0.1-SNAPSHOT.jar"]
EXPOSE 8010

# Копируем изображения
# COPY uploads/* /app/images/