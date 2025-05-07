package vn.com.picon.controller;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.picon.service.EmailService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/picon/mail")

public class EmailController {

    @GetMapping("/")
    public String handleContactSubmission() {
    return "/test.html";
    }

    @Autowired
    private EmailService emailService;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> handleContactSubmission(
            @RequestBody Map<String, String> request) { // Dùng @RequestBody
        String name = request.get("name");
        String email = request.get("email");
        String message = request.get("message");

        System.out.println("📬 Received CONTACT request: " + request);
        if (name == null || name.isBlank() || email == null || email.isBlank() || message == null || message.isBlank()) {
            System.out.println("⚠️ Invalid contact data: " + request);
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng cung cấp đầy đủ Tên, Email và Tin nhắn."));
        }

        try {
            emailService.sendContactEmail(name, email, message);
            return ResponseEntity.ok(Map.of("message", "Gửi liên hệ thành công!"));
        } catch (MessagingException e) {
            System.err.println("❌ Error sending contact email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi gửi email liên hệ."));
        }
    }
    @PostMapping("/application")
    public ResponseEntity<Map<String, String>> handleApplicationSubmission(

            @RequestPart("name") String name,
            @RequestPart("email") String email,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile) {

        System.out.println("📬 Received APPLICATION request. Name: " + name + ", Email: " + email + ", CV: " + (cvFile != null && !cvFile.isEmpty() ? cvFile.getOriginalFilename() : "N/A"));

        if (name == null || name.isBlank() || email == null || email.isBlank()) {

            System.out.println("⚠️ Invalid application data (missing name or email).");
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng cung cấp Tên và Email."));
        }


        InputStreamSource cvAttachment = null;
        String cvFileName = null;

        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                cvAttachment = new ByteArrayResource(cvFile.getBytes());
                cvFileName = cvFile.getOriginalFilename();
                System.out.println("Received CV file: " + cvFileName + ", size: " + cvFile.getSize());
            } catch (IOException e) {
                System.err.println("❌ Error reading uploaded CV file: " + e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi xử lý file CV tải lên."));
            }
        }

        try {
            emailService.sendApplicationEmail(name, email, phone, message, null);
            return ResponseEntity.ok(Map.of("message", "Hồ sơ ứng tuyển của bạn đã được gửi thành công!"));
        } catch (MessagingException e) {
            System.err.println("❌ Error sending application email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi hệ thống khi gửi hồ sơ ứng tuyển."));
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra
            System.err.println("❌ Unknown error processing application: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Lỗi không xác định trong quá trình xử lý."));
        }
    }
}