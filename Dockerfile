# Build stage: Construye el JAR dentro del contenedor
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app
COPY . .
# Usamos --no-daemon para entornos de CI/Docker
RUN gradle bootJar -x test --no-daemon

# Runtime stage: Imagen ligera para ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiamos el JAR (ajustado al nombre del nuevo proyecto)
COPY --from=build /app/build/libs/nathbit-business-core-0.0.1-SNAPSHOT.jar app.jar

# Configuración de memoria y Timezone (CRÍTICO para Costa Rica)
ENV JAVA_OPTS="-Xmx512m -Xms256m -Duser.timezone=America/Costa_Rica"
EXPOSE 8081

# Healthcheck para que Docker sepa si el micro se pegó
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8081/api/business/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]