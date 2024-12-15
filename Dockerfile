# Usa una imagen base de Java 17 (compatible con Kotlin y Spring Boot)
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia el archivo .jar a la imagen
COPY app-0.2024.1-SNAPSHOT.jar /app/app.jar

# Expone el puerto en el que corre tu aplicación (8080 por defecto en Spring Boot)
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "/app/app.jar"]
