# ============================================================
# EduHub 后端 Dockerfile
# 多阶段构建：1. Maven 编译 -> 2. JRE 运行
# ============================================================

# ---- 阶段1: 编译 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ---- 阶段2: 运行 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 时区
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

# 非root用户运行
RUN groupadd -r eduhub && useradd -r -g eduhub eduhub
USER eduhub

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-jar", "app.jar"]
