package az.starsoft.api.controller;

import az.starsoft.api.dto.DashboardStats;
import az.starsoft.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final BlogPostRepository blogPostRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ContactMessageRepository contactMessageRepository;

    @GetMapping
    public DashboardStats getDashboardStats() {
        return new DashboardStats(
                serviceRepository.count(),
                projectRepository.count(),
                blogPostRepository.count(),
                teamMemberRepository.count(),
                contactMessageRepository.count(),
                contactMessageRepository.countByReadFalse()
        );
    }
}
