package vn.com.picon.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from.address}")
    private String mailFromAddress;

    @Value("${app.mail.recipient.contact}")
    private String contactRecipientEmail;

    @Value("${app.mail.recipient.application}")
    private String applicationRecipientEmail;

    private Random random = new Random();
    private String verificationCode;
    private LocalDateTime expiryTime;

    public String generateVerificationCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    public void sendVerificationCode(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // true for multipart

        helper.setFrom(mailFromAddress); // <-- Sử dụng giá trị inject
        helper.setTo(toEmail);
        helper.setSubject("BaoNgoCV - Email Verification Code");
        helper.setText("Your verification code is: " + code);

        mailSender.send(message);
    }
    public void storeVerificationCode(String code) {
        this.verificationCode = code;
        this.expiryTime = LocalDateTime.now().plusSeconds(60);
    }

    public boolean verifyCode(String userInputCode) {
        if (verificationCode != null && verificationCode.equals(userInputCode)) {
            if (expiryTime != null && LocalDateTime.now().isBefore(expiryTime)) {
                return true;
            }
        }
        return false;
    }


    public void sendContactEmail(String name, String email, String messageContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(contactRecipientEmail);
        helper.setFrom(mailFromAddress);
        helper.setReplyTo(email); // Giữ nguyên để người nhận có thể trả lời trực tiếp người gửi
        helper.setSubject("PiconWebsite - Liên hệ mới từ: " + name);

        // ---- SỬA Ở ĐÂY ----
        // Tạo nội dung email thực tế từ các thông tin nhận được
        String emailText = String.format(
                "Bạn nhận được một tin nhắn liên hệ mới từ PiconWebsite:\n\n" +
                        "Tên người gửi: %s\n" +
                        "Email người gửi: %s\n\n" +
                        "Nội dung tin nhắn:\n%s",
                name, email, messageContent
        );

        // Đặt nội dung email đã được tạo đúng
        helper.setText(emailText, false); // false nghĩa là gửi dạng text thuần

        System.out.println("Chuẩn bị gửi email LIÊN HỆ tới " + contactRecipientEmail + " từ " + email);
        mailSender.send(message);
        System.out.println("Đã gửi email LIÊN HỆ.");
    }
    public void sendApplicationEmail(String name, String email, String phone, String messageContent,
                                     String cvFileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // <-- Sử dụng giá trị inject -->
        helper.setTo(applicationRecipientEmail);
        helper.setFrom(mailFromAddress); // <-- Sử dụng giá trị inject
        helper.setReplyTo(email);
        helper.setSubject("PiconWebsite - Hồ sơ ứng tuyển mới từ: " + name);

        StringBuilder emailBody = new StringBuilder();
        // ... xây dựng nội dung email ...
        helper.setText(emailBody.toString(), false);

        System.out.println("Chuẩn bị gửi email ỨNG TUYỂN tới " + applicationRecipientEmail + " từ " + email);
        mailSender.send(message);
        System.out.println("Đã gửi email ỨNG TUYỂN.");
    }
}