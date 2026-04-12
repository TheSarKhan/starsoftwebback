package az.starsoft.api.controller;

import az.starsoft.api.entity.TeamMember;
import az.starsoft.api.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamController {

    private final TeamMemberRepository teamMemberRepository;

    @GetMapping("/team")
    @Cacheable("team")
    public List<TeamMember> getActiveMembers() {
        return teamMemberRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    @GetMapping("/admin/team")
    public List<TeamMember> getAllMembers() {
        return teamMemberRepository.findAll();
    }

    @PostMapping("/admin/team")
    @CacheEvict(value = "team", allEntries = true)
    public TeamMember createMember(@RequestBody TeamMember member) {
        return teamMemberRepository.save(member);
    }

    @PutMapping("/admin/team/{id}")
    @CacheEvict(value = "team", allEntries = true)
    public ResponseEntity<TeamMember> updateMember(@PathVariable Long id, @RequestBody TeamMember member) {
        return teamMemberRepository.findById(id)
                .map(existing -> {
                    member.setId(id);
                    member.setCreatedAt(existing.getCreatedAt());
                    return ResponseEntity.ok(teamMemberRepository.save(member));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/team/{id}")
    @CacheEvict(value = "team", allEntries = true)
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        teamMemberRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
