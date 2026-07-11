# =====================================================================
# Dockerfile production-grade — Task Manager Pro
# Multi-stage: build (JDK) → runtime (JRE leve, non-root, com healthcheck)
# =====================================================================

# ---------------------------------------------------------------------
# STAGE 1: BUILD
# Usa JDK completo para compilar backend (Maven) e frontend (Vite)
# ---------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copia primeiro os arquivos de dependência para aproveitar cache de camadas
COPY package.json package-lock.json ./
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Instala dependências do Node e compila o frontend (gera static/dist/)
RUN npm ci
RUN npm run build

# Copia o resto do código-fonte e compila o backend
COPY src ./src

# Dá permissão de execução ao wrapper e empacota (testes rodam no CI, não aqui)
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# ---------------------------------------------------------------------
# STAGE 2: RUNTIME
# JRE leve (~40% menor que JDK). Non-root, com HEALTHCHECK.
# ---------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Instala wget (necessário para o HEALTHCHECK no Alpine)
RUN apk add --no-cache wget

# Cria usuário non-root por segurança
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

# Copia SOMENTE o jar do stage de build (sem Maven, sem src, sem node_modules)
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Muda para usuário não-privilegiado
USER app

# Porta da aplicação (bate com application.properties: server.port=8081)
EXPOSE 8081

# Health check: bate no Actuator a cada 30s
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# JVM flags otimizadas para container
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
