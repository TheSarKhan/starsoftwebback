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
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );
    private static final long MAX_SIZE = 20 * 1024 * 1024; // 20 MB

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
        String ext = getExtension(file.getOriginalFilename());
        String contentType = file.getContentType();
        boolean validByType = contentType != null && ALLOWED_TYPES.contains(contentType.toLowerCase());
        boolean validByExtension = ALLOWED_EXTENSIONS.contains(ext);
        if (!validByType && !validByExtension) {
            return ResponseEntity.badRequest().body(Map.of("error", "Yalnız JPEG, PNG, WebP və GIF icazəlidir."));
        }
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
            log.error("Image upload failed — dir={} file={} cause={}", uploadDir, filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "upload_failed",
                    "message", "Yükləmə uğursuz oldu: " + e.getMessage()
            ));
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
        String ext = dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
        return ALLOWED_EXTENSIONS.contains(ext) ? ext : ".jpg";
    }
}
