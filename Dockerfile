FROM maven:3.8.3-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app
# 复制项目的 pom.xml 和源代码
COPY pom.xml .
COPY src ./src
# 使用 Maven 构建项目
RUN mvn clean package -DskipTests
#运行
CMD ["java","-jar","/app/target/user-center-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]

