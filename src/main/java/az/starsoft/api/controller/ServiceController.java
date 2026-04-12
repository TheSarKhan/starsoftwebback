package az.starsoft.api.controller;

import az.starsoft.api.entity.Service;
import az.starsoft.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;

    @GetMapping("/services")
    @Cacheable("services")
    public List<Service> getActiveServices() {
        return serviceRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @GetMapping("/admin/services")
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @PostMapping("/admin/services")
    @CacheEvict(value = "services", allEntries = true)
    public Service createService(@RequestBody Service service) {
        return serviceRepository.save(service);
    }

    @PutMapping("/admin/services/{id}")
    @CacheEvict(value = "services", allEntries = true)
    public ResponseEntity<Service> updateService(@PathVariable Long id, @RequestBody Service service) {
        return serviceRepository.findById(id)
                .map(existing -> {
                    service.setId(id);
                    service.setCreatedAt(existing.getCreatedAt());
                    return ResponseEntity.ok(serviceRepository.save(service));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/services/{id}")
    @CacheEvict(value = "services", allEntries = true)
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
