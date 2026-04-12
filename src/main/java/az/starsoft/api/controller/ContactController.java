package az.starsoft.api.controller;

import az.starsoft.api.config.RateLimiter;
import az.starsoft.api.entity.ContactMessage;
import az.starsoft.api.repository.ContactMessageRepository;
import az.starsoft.api.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;
    private final RateLimiter rateLimiter;
    private final MailService mailService;

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> submitMessage(
            @Valid @RequestBody ContactMessage message,
            HttpServletRequest request) {
        String clientKey = clientIp(request);
        if (!rateLimiter.allowContact(clientKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "error", "rate_limited",
                    "message", "Çox sorğu göndərdiniz. Bir saatdan sonra yenidən cəhd edin və ya birbaşa email/WhatsApp ilə əlaqə saxlayın."
            ));
        }
        // Always start fresh server-side — don't trust client-supplied id/createdAt/read flags.
        message.setId(null);
        message.setRead(false);
        message.setCreatedAt(null);
        ContactMessage saved = contactMessageRepository.save(message);
        mailService.sendContactNotification(saved);
        return ResponseEntity.ok(Map.of("message", "Mesajınız uğurla göndərildi!"));
    }

    @GetMapping("/admin/contacts")
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @PutMapping("/admin/contacts/{id}/read")
    public ResponseEntity<ContactMessage> markAsRead(@PathVariable Long id) {
        return contactMessageRepository.findById(id)
                .map(msg -> {
                    msg.setRead(true);
                    return ResponseEntity.ok(contactMessageRepository.save(msg));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/contacts/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        contactMessageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
