package az.starsoft.api.controller;

import az.starsoft.api.entity.SiteSetting;
import az.starsoft.api.repository.SiteSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SiteSettingRepository settingRepository;

    @GetMapping("/settings")
    @Cacheable("settings")
    public Map<String, String> getPublicSettings() {
        return settingRepository.findAll().stream()
                .collect(Collectors.toMap(SiteSetting::getSettingKey, SiteSetting::getSettingValue));
    }

    @GetMapping("/admin/settings")
    public List<SiteSetting> getAllSettings() {
        return settingRepository.findAll();
    }

    @PutMapping("/admin/settings")
    @CacheEvict(value = "settings", allEntries = true)
    public ResponseEntity<SiteSetting> updateSetting(@RequestBody SiteSetting setting) {
        return settingRepository.findBySettingKey(setting.getSettingKey())
                .map(existing -> {
                    existing.setSettingValue(setting.getSettingValue());
                    return ResponseEntity.ok(settingRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.ok(settingRepository.save(setting)));
    }
}
