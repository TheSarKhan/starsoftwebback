package az.starsoft.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
    private long totalServices;
    private long totalProjects;
    private long totalBlogPosts;
    private long totalTeamMembers;
    private long totalMessages;
    private long unreadMessages;
}
