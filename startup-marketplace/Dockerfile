# Etapa 1: Compilar el proyecto con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el pom.xml y el código fuente
COPY pom.xml .
COPY src ./src
# Compilamos ignorando los tests para que el despliegue sea rápido
RUN mvn clean package -DskipTests

# Etapa 2: Crear la imagen final súper ligera
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el archivo .jar generado en la Etapa 1
COPY --from=build /app/target/*.jar app.jar
# Exponemos el puerto por defecto de Spring Boot
EXPOSE 8080
# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]