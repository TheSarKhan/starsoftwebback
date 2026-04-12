package az.starsoft.api.service;

import az.starsoft.api.entity.ContactMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sends notification emails. Currently used to alert the founder when a new contact
 * message lands. Failures are logged but never propagated — a flaky SMTP must not
 * cause the contact form to return 500 to the visitor.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;
    private final String notifyTo;
    private final boolean enabled;

    public MailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String from,
            @Value("${app.mail.from-name:StarSoft}") String fromName,
            @Value("${app.mail.notify-to:}") String notifyTo,
            @Value("${app.mail.enabled:true}") boolean enabled
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
        this.notifyTo = notifyTo;
        this.enabled = enabled;
    }

    @Async
    public void sendContactNotification(ContactMessage msg) {
        if (!enabled) {
            log.debug("Mail disabled — skipping contact notification");
            return;
        }
        if (from == null || from.isBlank() || notifyTo == null || notifyTo.isBlank()) {
            log.warn("Mail from/notifyTo not configured — skipping contact notification");
            return;
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(notifyTo);
            helper.setReplyTo(msg.getEmail());
            helper.setSubject("[StarSoft] Yeni mesaj: " + safe(msg.getSubject(), "(mövzu yoxdur)"));
            helper.setText(buildBody(msg), false);
            mailSender.send(mime);
            log.info("Contact notification sent to {}", notifyTo);
        } catch (MessagingException | UnsupportedEncodingException | RuntimeException e) {
            log.error("Failed to send contact notification", e);
        }
    }

    private String buildBody(ContactMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("StarSoft.az saytından yeni mesaj.\n\n");
        sb.append("Ad:      ").append(safe(msg.getName(), "—")).append('\n');
        sb.append("Email:   ").append(safe(msg.getEmail(), "—")).append('\n');
        sb.append("Telefon: ").append(safe(msg.getPhone(), "—")).append('\n');
        sb.append("Mövzu:   ").append(safe(msg.getSubject(), "—")).append('\n');
        sb.append("Tarix:   ").append(TS.format(LocalDateTime.now())).append('\n');
        sb.append("\n--- Mesaj ---\n");
        sb.append(safe(msg.getMessage(), "")).append('\n');
        return sb.toString();
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
