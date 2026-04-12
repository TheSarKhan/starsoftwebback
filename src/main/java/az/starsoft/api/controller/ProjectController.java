package az.starsoft.api.controller;

import az.starsoft.api.entity.Project;
import az.starsoft.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;

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

    @PostMapping("/admin/projects")
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public Project createProject(@RequestBody Project project) {
        return projectRepository.save(project);
    }

    @PutMapping("/admin/projects/{id}")
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        return projectRepository.findById(id)
                .map(existing -> {
                    project.setId(id);
                    project.setCreatedAt(existing.getCreatedAt());
                    return ResponseEntity.ok(projectRepository.save(project));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/projects/{id}")
    @CacheEvict(value = {"projects", "featuredProjects"}, allEntries = true)
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
