package az.starsoft.api.config;

import az.starsoft.api.entity.*;
import az.starsoft.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final BlogPostRepository blogPostRepository;
    private final SiteSettingRepository settingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedServices();
        seedProjects();
        seedTeam();
        seedBlogPosts();
        seedSettings();
    }

    private void seedAdmin() {
        if (userRepository.count() > 0) return;
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD is not set — skipping admin user seeding. "
                    + "Set ADMIN_PASSWORD env var on first run to create the admin account.");
            return;
        }
        userRepository.save(User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .email(adminEmail)
                .role("ADMIN")
                .build());
        log.info("Seeded admin user '{}'", adminUsername);
    }

    private void seedServices() {
        if (serviceRepository.count() > 0) return;

        serviceRepository.save(Service.builder()
                .title("Web development").titleAz("Web development")
                .description("Fast, SEO-friendly websites and web apps built for conversion.")
                .descriptionAz("Sürətli, SEO dostu və konversiyaya hesablanmış müasir veb saytlar və veb tətbiqlər.")
                .icon("Globe").sortOrder(1).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Mobile app").titleAz("Mobil tətbiq")
                .description("iOS and Android apps from a single codebase — less cost, faster delivery.")
                .descriptionAz("iOS və Android üçün eyni keyfiyyətdə işləyən mobil tətbiqlər.")
                .icon("Smartphone").sortOrder(2).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Cybersecurity").titleAz("Kibertəhlükəsizlik")
                .description("Audit, penetration testing and continuous protection for your business.")
                .descriptionAz("Audit, penetrasiya testi və davamlı qoruma — biznesinizi və müştəri məlumatınızı qoruyun.")
                .icon("ShieldCheck").sortOrder(3).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Infrastructure").titleAz("İnfrastruktur")
                .description("Your site and systems stay online, fast and monitored — server, hosting, alerting.")
                .descriptionAz("Saytınız və sistemləriniz dayanmadan, sürətlə işləsin — server, hosting, monitorinq.")
                .icon("Server").sortOrder(4).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Automation").titleAz("Avtomatlaşdırma")
                .description("Turn manual processes into systems — win back 20+ hours a week.")
                .descriptionAz("Manual prosesləri sistemlərə çevirin — həftədə 20+ saatınızı geri qazanın.")
                .icon("Zap").sortOrder(5).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Business analytics").titleAz("Biznes analitika")
                .description("Dashboards and reporting systems that turn data into decisions.")
                .descriptionAz("Məlumatınızı qərara çevirən dashboard və hesabat sistemləri.")
                .icon("BarChart3").sortOrder(6).active(true).build());

        serviceRepository.save(Service.builder()
                .title("Telegram bots").titleAz("Telegram botlar")
                .description("Custom Telegram bots for orders, support, notifications and business automation.")
                .descriptionAz("Sifariş qəbulu, müştəri dəstəyi, bildirişlər və biznes avtomatlaşdırması üçün Telegram botlar.")
                .icon("Bot").sortOrder(7).active(true).build());

        log.info("Seeded 7 services");
    }

    private void seedProjects() {
        if (projectRepository.count() > 0) return;

        projectRepository.save(Project.builder()
                .title("testup.az")
                .description("Müasir imtahan və test platforması — sıfırdan qurulub, davamlı dəstəklənir. Fikirdən canlı məhsula, sabit qiymətlə.")
                .category("Web platforma")
                .technologies("Next.js, Spring Boot, PostgreSQL, Docker, REST API")
                .projectUrl("https://testup.az")
                .featured(true).active(true).build());

        log.info("Seeded 1 project");
    }

    private void seedTeam() {
        if (teamMemberRepository.count() > 0) return;

        teamMemberRepository.save(TeamMember.builder()
                .fullName("Sərxan Babayev")
                .position("Founder & Technical Lead").positionAz("Qurucu & Texniki Rəhbər")
                .bio("Müştərilərlə birbaşa işləyir, layihələrin texniki istiqamətini idarə edir.")
                .sortOrder(1).active(true).build());

        log.info("Seeded 1 team member");
    }

    private void seedBlogPosts() {
        if (blogPostRepository.count() > 0) return;

        blogPostRepository.save(BlogPost.builder()
                .title("Niyə hər biznesin veb sayta ehtiyacı var?")
                .slug("niye-her-biznesin-veb-sayta-ehtiyaci-var")
                .summary("Instagram səhifəsi kifayət deyil. Müasir müştəri sizi Google-da axtarır — tapılmırsınızsa, mövcud deyilsiniz.")
                .content("<p>Azərbaycanda bir çox kiçik biznes hələ də yalnız Instagram səhifəsi ilə işləyir. Amma müştərilərin 70%-dən çoxu yeni xidmət axtaranda Google-a yazır. Əgər orada yoxsunuzsa — rəqibiniz tapılır.</p><h2>Veb sayt nə verir?</h2><p><strong>1. Etibarlılıq.</strong> Saytı olan şirkət daha ciddi qəbul edilir. Vizitka, portfolio, əlaqə — hamısı bir yerdə.</p><p><strong>2. 7/24 satış.</strong> Saytınız gecə-gündüz işləyir. Siz yatanda da müştəri sizin haqqınızda oxuyur.</p><p><strong>3. SEO — pulsuz müştəri.</strong> Google-da görünmək reklam xərci olmadan müştəri gətirir.</p><p><strong>4. Nəzarət.</strong> Instagram algoritmi dəyişir — saytınız sizindir, heç kim onu sizə bağlaya bilməz.</p><h2>Nə etməli?</h2><p>Sadə korporativ sayt 1-2 həftəyə hazır olur. StarSoft olaraq biz sabit qiymətə, admin panelli, SEO dostu saytlar qururuq. İlk konsultasiya pulsuzdur.</p>")
                .tags("web,sayt,biznes,SEO")
                .author("StarSoft")
                .published(true)
                .publishedAt(java.time.LocalDateTime.now().minusDays(3))
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Telegram bot biznesinizə necə kömək edə bilər?")
                .slug("telegram-bot-biznesinize-nece-komek-ede-biler")
                .summary("Sifariş qəbulundan müştəri dəstəyinə qədər — Telegram bot əl işini azaldır, müştəri məmnuniyyətini artırır.")
                .content("Azərbaycanda Telegram istifadəçi sayı sürətlə artır. Biznes üçün bu bir fürsətdir — müştəriləriniz artıq oradadır.\n\n## Telegram bot nə edə bilər?\n\n**1. Sifariş qəbulu.** Müştəri bot vasitəsilə məhsul seçir, sifariş verir. Siz admin paneldən izləyirsiniz.\n\n**2. Avtomatik cavab.** Ən çox verilən suallara bot dərhal cavab verir — siz hər mesajı əl ilə yazmırsınız.\n\n**3. Bildirişlər.** Yeni sifariş, ödəniş, çatdırılma statusu — müştəriyə avtomatik Telegram bildirişi gedir.\n\n**4. CRM inteqrasiya.** Bot məlumatları birbaşa sisteminizə yazır — əl ilə köçürmə lazım deyil.\n\n## Kimə lazımdır?\n\n- Restoran və kafe (menyu + sifariş)\n- Online mağaza (katalog + sifariş)\n- Xidmət şirkətləri (rezervasiya + xatırlatma)\n- Təhsil (qeydiyyat + nəticə bildirişi)\n\nStarSoft sadə FAQ botdan mürəkkəb CRM botuna qədər hər həll üçün hazırdır.")
                .tags("telegram,bot,avtomatlaşdırma,biznes")
                .author("StarSoft")
                .published(true)
                .publishedAt(java.time.LocalDateTime.now().minusDays(1))
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Kibertəhlükəsizlik: kiçik bizneslər üçün 5 sadə addım")
                .slug("kibertehlukesizlik-kicik-biznesler-ucun-5-sade-addim")
                .summary("Haker yalnız böyük şirkətləri hədəf almır. Kiçik bizneslər daha asan hədəfdir — amma qorunmaq çətin deyil.")
                .content("Azərbaycanda kiçik bizneslər tez-tez düşünür: 'Bizi kim hack edəcək ki?' Amma statistikaya görə kiber hücumların 43%-i kiçik bizneslərə yönəlir — çünki onlar daha az qorunur.\n\n## 5 sadə addım:\n\n**1. Güclü şifrə + 2FA.** Bütün hesablarda iki faktorlu autentifikasiya aktiv edin. Bu bir dəqiqəlik işdir.\n\n**2. Yeniləmələri gecikdirməyin.** Sistem və proqram yeniləmələri təhlükəsizlik boşluqlarını bağlayır.\n\n**3. Backup qurun.** Həftəlik avtomatik backup — ransomware hücumundan yeganə sığorta.\n\n**4. Komandanı öyrədin.** Phishing e-poçtlarını tanımaq təlimi — ən effektiv qorunma.\n\n**5. SSL sertifikat.** Saytınız https ilə açılmalıdır — həm təhlükəsizlik, həm SEO üçün.\n\nBunlar əsas addımlardır. Daha dərin audit lazımdırsa — StarSoft təhlükəsizlik xidməti ilə tam yoxlama keçirə bilərsiniz.")
                .tags("təhlükəsizlik,kibertəhlükəsizlik,biznes,hack")
                .author("StarSoft")
                .published(true)
                .publishedAt(java.time.LocalDateTime.now())
                .build());

        log.info("Seeded 3 blog posts");
    }

    private void seedSettings() {
        if (settingRepository.count() > 0) return;

        settingRepository.save(SiteSetting.builder()
                .settingKey("site_title").settingValue("StarSoft")
                .description("Site title").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("site_description").settingValue("Texnologiyanızı ulduzlara çatdırırıq")
                .description("Site tagline").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("contact_email").settingValue("sarxanbabayevcontact@gmail.com")
                .description("Contact email").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("contact_phone").settingValue("+994 50 201 71 64")
                .description("Contact phone").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("contact_whatsapp").settingValue("+994 50 201 71 64")
                .description("WhatsApp number").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("address").settingValue("Bakı, Azərbaycan")
                .description("Office address").build());
        settingRepository.save(SiteSetting.builder()
                .settingKey("working_hours").settingValue("B.e – Cümə, 09:00 – 18:00")
                .description("Working hours").build());

        log.info("Seeded 7 site settings");
    }
}
