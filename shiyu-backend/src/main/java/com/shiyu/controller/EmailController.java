package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.config.EmailConfig;
import com.shiyu.service.ai.EmailConfigPersistence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Tag(name = "邮件设置", description = "SMTP 邮件配置管理")
@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailConfig emailConfig;
    private final EmailConfigPersistence persistence;

    public EmailController(EmailConfig emailConfig, EmailConfigPersistence persistence) {
        this.emailConfig = emailConfig;
        this.persistence = persistence;
    }

    @Operation(summary = "获取邮件配置", description = "返回当前 SMTP 配置（密码脱敏）")
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", emailConfig.isEnabled());
        config.put("host", emailConfig.getHost());
        config.put("port", emailConfig.getPort());
        config.put("username", emailConfig.getUsername());
        config.put("password", maskPassword(emailConfig.getPassword()));
        config.put("fromName", emailConfig.getFromName());
        return ApiResponse.success(config);
    }

    @Operation(summary = "保存邮件配置", description = "保存 SMTP 配置")
    @PostMapping("/config")
    public ApiResponse<String> saveConfig(@RequestBody Map<String, Object> body) {
        if (body.containsKey("enabled")) emailConfig.setEnabled((Boolean) body.get("enabled"));
        if (body.containsKey("host")) emailConfig.setHost((String) body.get("host"));
        if (body.containsKey("port")) emailConfig.setPort((Integer) body.get("port"));
        if (body.containsKey("username")) emailConfig.setUsername((String) body.get("username"));
        if (body.containsKey("password")) {
            String pwd = (String) body.get("password");
            if (pwd != null && !pwd.contains("****")) {
                emailConfig.setPassword(pwd);
            }
        }
        if (body.containsKey("fromName")) emailConfig.setFromName((String) body.get("fromName"));
        persistence.save();
        return ApiResponse.success("配置已保存");
    }

    @Operation(summary = "测试邮件发送", description = "发送测试邮件验证 SMTP 配置")
    @PostMapping("/test")
    public ApiResponse<Map<String, Object>> testEmail(@RequestBody Map<String, String> body) {
        String toEmail = body.get("email");
        if (toEmail == null || toEmail.isBlank()) {
            return ApiResponse.error("请输入测试邮箱地址");
        }

        long start = System.currentTimeMillis();
        try {
            JavaMailSenderImpl sender = createMailSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailConfig.getUsername());
            message.setTo(toEmail);
            message.setSubject("食遇 - 邮件配置测试");
            message.setText("恭喜！您的邮件配置成功。\n\n发送时间：" + java.time.LocalDateTime.now() + "\n\n此邮件由食遇系统自动发送。");
            sender.send(message);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "测试邮件已发送到 " + toEmail);
            result.put("responseTime", System.currentTimeMillis() - start);
            return ApiResponse.success(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "发送失败：" + e.getMessage());
            return ApiResponse.success(result);
        }
    }

    private JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(emailConfig.getHost());
        sender.setPort(emailConfig.getPort());
        sender.setUsername(emailConfig.getUsername());
        sender.setPassword(emailConfig.getPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        return sender;
    }

    private String maskPassword(String pwd) {
        if (pwd == null || pwd.isBlank()) return "";
        if (pwd.length() < 8) return "****";
        return pwd.substring(0, 3) + "****" + pwd.substring(pwd.length() - 3);
    }
}
