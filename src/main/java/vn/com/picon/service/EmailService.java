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
                                     InputStreamSource cvAttachment, String cvFileName) throws MessagingException { // Thêm InputStreamSource cvAttachment
        MimeMessage message = mailSender.createMimeMessage();
        // true = multipart message (cần thiết cho attachment)
        // "UTF-8" = encoding
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(applicationRecipientEmail); // Địa chỉ email nhận hồ sơ
        helper.setFrom(mailFromAddress);         // Email gửi đi của bạn
        helper.setReplyTo(email);                // Để người nhận có thể reply trực tiếp cho ứng viên
        helper.setSubject("PiconWebsite - Hồ sơ ứng tuyển mới từ: " + name);

        // Xây dựng nội dung email
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Bạn nhận được một hồ sơ ứng tuyển mới từ PiconWebsite:\n\n");
        emailBody.append("Tên ứng viên: ").append(name).append("\n");
        emailBody.append("Email ứng viên: ").append(email).append("\n");

        if (phone != null && !phone.isBlank()) {
            emailBody.append("Số điện thoại: ").append(phone).append("\n");
        }

        emailBody.append("\nLời nhắn của ứng viên:\n");
        if (messageContent != null && !messageContent.isBlank()) {
            emailBody.append(messageContent).append("\n");
        } else {
            emailBody.append("(Không có lời nhắn)\n");
        }

        if (cvAttachment != null && cvFileName != null && !cvFileName.isBlank()) {
            emailBody.append("\nCV được đính kèm với tên file: ").append(cvFileName);
            helper.addAttachment(cvFileName, cvAttachment); // Đính kèm file CV
            System.out.println("Đã thêm CV đính kèm: " + cvFileName);
        } else {
            emailBody.append("\n(Không có CV đính kèm)");
        }

        helper.setText(emailBody.toString(), false); // false nghĩa là gửi dạng text thuần

        System.out.println("Chuẩn bị gửi email ỨNG TUYỂN tới " + applicationRecipientEmail + " từ " + email + (cvFileName != null ? " với CV: " + cvFileName : ""));
        mailSender.send(message);
        System.out.println("Đã gửi email ỨNG TUYỂN.");
    }

}
