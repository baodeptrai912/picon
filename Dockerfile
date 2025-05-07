# STAGE 1: Build the application using Maven
# Sử dụng một base image có sẵn JDK 17 và Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

# Đặt thư mục làm việc trong container
WORKDIR /app

# Sao chép file pom.xml trước để tận dụng Docker layer caching
# Nếu file pom.xml không đổi, Docker sẽ dùng lại layer này, tiết kiệm thời gian download dependencies
COPY pom.xml .

# Download tất cả dependencies (không cần chạy build ngay)
# Tùy chọn: bạn có thể chạy `mvn dependency:go-offline` để tải hết dependency
# Hoặc để đơn giản, cứ để Maven tự xử lý khi package
# RUN mvn dependency:go-offline -B

# Sao chép toàn bộ mã nguồn của dự án vào thư mục làm việc trong container
COPY src ./src

# Build ứng dụng, tạo file .jar. Bỏ qua test để build nhanh hơn trên CI.
# File JAR sẽ được tạo trong thư mục /app/target/
RUN mvn clean package -DskipTests

# STAGE 2: Create a smaller image to run the application
# Sử dụng một base image Java runtime nhỏ gọn hơn (chỉ chứa JRE)
FROM eclipse-temurin:17-jre-alpine

# Đặt thư mục làm việc
WORKDIR /app

# Sao chép file JAR đã được build từ stage 'builder' vào image hiện tại
# Thay 'picon-0.0.1-SNAPSHOT.jar' nếu artifactId hoặc version của bạn khác
COPY --from=builder /app/target/picon-0.0.1-SNAPSHOT.jar app.jar

# Expose cổng mà ứng dụng Spring Boot của bạn lắng nghe (mặc định là 8080)
# Render sẽ tự động map cổng này.
EXPOSE 8080

# Lệnh để khởi chạy ứng dụng khi container được start
ENTRYPOINT ["java", "-jar", "app.jar"]