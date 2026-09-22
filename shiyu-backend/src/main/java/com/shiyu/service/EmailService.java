package com.shiyu.service;

import com.shiyu.config.EmailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailConfig emailConfig;

    public EmailService(EmailConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    public void sendVerificationCode(String toEmail, String code, String purpose) {
        String subject = "食遇 - 邮箱验证码";
        String purposeText = "reset_password".equals(purpose) ? "重置密码" : "验证邮箱";
        String text = "您正在进行【" + purposeText + "】操作。\n\n验证码：" + code + "\n\n验证码 5 分钟内有效，请勿泄露给他人。\n如果不是您本人操作，请忽略此邮件。";

        if (!emailConfig.isEnabled()) {
            log.info("=== EMAIL VERIFICATION CODE (dev mode) === To: {} Code: {} ====================", toEmail, code);
            return;
        }

        if (emailConfig.getUsername() == null || emailConfig.getUsername().isBlank()) {
            log.error("SMTP username not configured, cannot send email");
            log.info("Verification code for {}: {}", toEmail, code);
            return;
        }

        try {
            JavaMailSenderImpl sender = createMailSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getUsername());
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(text);
            sender.send(message);
            log.info("Verification code sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            log.info("Verification code for {}: {}", toEmail, code);
        }
    }

    private JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(emailConfig.getHost());
        sender.setPort(emailConfig.getPort());
        sender.setUsername(emailConfig.getUsername());
        sender.setPassword(emailConfig.getPassword());
        sender.setDefaultEncoding("UTF-8");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        sender.setJavaMailProperties(props);
        return sender;
    }
}
