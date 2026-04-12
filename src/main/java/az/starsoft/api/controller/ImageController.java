package az.starsoft.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/images")
@Slf4j
public class ImageController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Path uploadDir;

    public ImageController(@Value("${app.upload.dir:images}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fayl seçilməyib."));
        }
        if (file.getSize() > MAX_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fayl 5 MB-dan böyükdür."));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Yalnız JPEG, PNG, WebP və GIF icazəlidir."));
        }

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;

        try {
            Path target = uploadDir.resolve(filename).normalize();
            // Prevent path traversal
            if (!target.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yanlış fayl adı."));
            }
            file.transferTo(target);
            log.info("Image uploaded: {}", filename);
            String url = "/images/" + filename;
            return ResponseEntity.ok(Map.of("url", url, "filename", filename));
        } catch (IOException e) {
            log.error("Image upload failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Yükləmə uğursuz oldu."));
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<?> delete(@PathVariable String filename) {
        // Sanitize — only allow UUID-style filenames with image extensions
        if (!filename.matches("^[a-f0-9\\-]+\\.(jpg|jpeg|png|webp|gif)$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Yanlış fayl adı."));
        }
        try {
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yanlış fayl adı."));
            }
            Files.deleteIfExists(target);
            log.info("Image deleted: {}", filename);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("Image delete failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Silmə uğursuz oldu."));
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
    }
}
