package az.starsoft.api.repository;

import az.starsoft.api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByActiveTrueOrderByCreatedAtDesc();
    List<Project> findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();
    List<Project> findByCategoryAndActiveTrueOrderByCreatedAtDesc(String category);
}
