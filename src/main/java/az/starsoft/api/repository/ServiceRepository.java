package az.starsoft.api.repository;

import az.starsoft.api.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrueOrderBySortOrderAsc();
}
