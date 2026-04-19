package az.starsoft.api.controller;

import az.starsoft.api.entity.Project;
import az.starsoft.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectRepository projectRepository;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    @Value("${app.upload.dir:images}")
    private String uploadDirPath;

    @GetMapping("/projects")
    @Cacheable("projects")
    public List<Project> getActiveProjects() {
        return projectRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/projects/featured")
    @Cacheable("featuredProjects")
    public List<Project> getFeaturedProjects() {
        return projectRepository.findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();
    }

    @GetMapping("/admin/projects")
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PostMapping(value = "/admin/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public ResponseEntity<?> createProject(
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam(required = false, defaultValue = "Web") String category,
            @RequestParam(required = false, defaultValue = "") String clientName,
            @RequestParam(required = false, defaultValue = "") String projectUrl,
            @RequestParam(required = false, defaultValue = "") String technologies,
            @RequestParam(required = false, defaultValue = "") String imageUrl,
            @RequestParam(required = false, defaultValue = "false") boolean featured,
            @RequestParam(required = false, defaultValue = "true") boolean active,
            @RequestParam(required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                imageUrl = storeImage(image);
            }
            Project project = Project.builder()
                    .title(title).description(description).category(category)
                    .clientName(clientName).projectUrl(projectUrl)
                    .technologies(technologies).imageUrl(imageUrl)
                    .featured(featured).active(active).build();
            return ResponseEntity.ok(projectRepository.save(project));
        } catch (Exception e) {
            log.error("Create project failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping(value = "/admin/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false, defaultValue = "") String description,
            @RequestParam(required = false, defaultValue = "Web") String category,
            @RequestParam(required = false, defaultValue = "") String clientName,
            @RequestParam(required = false, defaultValue = "") String projectUrl,
            @RequestParam(required = false, defaultValue = "") String technologies,
            @RequestParam(required = false, defaultValue = "") String imageUrl,
            @RequestParam(required = false, defaultValue = "false") boolean featured,
            @RequestParam(required = false, defaultValue = "true") boolean active,
            @RequestParam(required = false) MultipartFile image) {
        return projectRepository.findById(id).map(existing -> {
            try {
                String finalImageUrl = imageUrl;
                if (image != null && !image.isEmpty()) {
                    finalImageUrl = storeImage(image);
                } else if (finalImageUrl.isEmpty()) {
                    finalImageUrl = existing.getImageUrl();
                }
                existing.setTitle(title);
                existing.setDescription(description);
                existing.setCategory(category);
                existing.setClientName(clientName);
                existing.setProjectUrl(projectUrl);
                existing.setTechnologies(technologies);
                existing.setImageUrl(finalImageUrl);
                existing.setFeatured(featured);
                existing.setActive(active);
                return ResponseEntity.ok(projectRepository.save(existing));
            } catch (Exception e) {
                log.error("Update project failed: {}", e.getMessage(), e);
                return ResponseEntity.<Project>internalServerError().build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/projects/{id}")
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String storeImage(MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) throw new IOException("Invalid filename");
        file.transferTo(target);
        log.info("Image stored: {}", filename);
        return "/images/" + filename;
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        String ext = dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
        return ALLOWED_EXTENSIONS.contains(ext) ? ext : ".jpg";
    }
}
