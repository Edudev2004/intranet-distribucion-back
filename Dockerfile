# Etapa 1: Compilación con Gradle
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de configuración de Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle gradle.properties ./
RUN chmod +x gradlew

# Copiar código fuente y compilar JAR ejecutable
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: Imagen final ligera para producción
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el artefacto compilado
COPY --from=builder /app/build/libs/app.jar app.jar

# Configuración de puerto y memoria optimizada para el plan Free de Render (512 MB)
ENV PORT=8080
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
