# ============================================================================
# 后端镜像 — Multi-stage build
#   1. builder: Maven 编译 → fat jar
#   2. runtime: JRE 仅运行 jar
# ============================================================================

# --- Stage 1: build ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先拷 pom + mvnw,利用 docker 层缓存先拉依赖
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn ./.mvn
RUN ./mvnw -B dependency:go-offline -q || true

# 拷源码再编译
COPY src ./src
RUN ./mvnw -B clean package -DskipTests -q

# --- Stage 2: runtime ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# 拷 fat jar(target 下唯一 *.jar)
COPY --from=builder /build/target/*.jar /app/app.jar

# uploads/ 目录(合同附件)由 docker-compose volume 挂入,容器内只创建挂载点
RUN mkdir -p /app/uploads

EXPOSE 8080

# Spring Boot 配置由环境变量覆盖(docker-compose 注入 SPRING_DATASOURCE_URL 等)
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
