package vn.com.picon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả các đường dẫn (hoặc "/picon/mail/**")
                .allowedOrigins("https://picon-frontend.vercel.app") // Cho phép nguồn gốc này
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Cho phép các phương thức này (bao gồm cả OPTIONS cho preflight)
                .allowedHeaders("*") // Cho phép tất cả các header
                .allowCredentials(false); // Đặt là true nếu bạn cần gửi cookie/authentication header
        // .maxAge(3600); // Thời gian cache kết quả preflight (giây)
    }
}
