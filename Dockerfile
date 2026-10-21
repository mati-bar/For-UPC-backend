
# ==========================================
# ETAPA 1: COMPILAR CON JAVA 21
# ==========================================

FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar Maven Wrapper y pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Dar permisos al Maven Wrapper
RUN chmod +x mvnw

# Descargar dependencias
RUN ./mvnw dependency:go-offline -B

# Copiar codigo fuente
COPY src/ src/

# Compilar y generar el JAR
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# ETAPA 2: EJECUTAR BACKEND
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar JAR generado
COPY --from=build /app/target/For-Upc_backend-0.0.1-SNAPSHOT.jar app.jar

# Puerto de Render
EXPOSE 10000

# Ejecutar Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
