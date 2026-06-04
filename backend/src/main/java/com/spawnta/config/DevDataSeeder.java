package com.spawnta.config;

import com.spawnta.entity.*;
import com.spawnta.repository.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * Seeds development data on every startup (dev profile).
 * All listed accounts use password {@value #DEV_PASSWORD} and are email-verified.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
@Order(100)
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    static final String DEV_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserAchievementRepository achievementRepository;
    private final UserLevelHistoryRepository levelHistoryRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public DevDataSeeder(UserRepository userRepository,
                         BadgeRepository badgeRepository,
                         UserAchievementRepository achievementRepository,
                         UserLevelHistoryRepository levelHistoryRepository,
                         ActivityRepository activityRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.achievementRepository = achievementRepository;
        this.levelHistoryRepository = levelHistoryRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DevDataSeeder: ensuring demo data ===");

        List<Badge> starterBadges = ensureBadges();

        upsertDevAdmin("admin@spawnta.com", "Admin", "Spawnta");

        User demo = upsertDevUser("demo@spawnta.com", "Ayman", "Demo", u -> {
            u.setXp(750);
            u.setLevel(3);
            u.setTotalXpEarned(2750);
            u.setCurrentLevelXpRequired(1000);
            u.setInterests(EnumSet.of(Interest.HIKING, Interest.TRAVEL, Interest.PHOTOGRAPHY, Interest.COOKING));
            u.setBio("Passionné de randonnée et de voyages");
        });
        awardStarterBadges(demo, starterBadges);
        ensureLevelHistory(demo, new int[][]{{1, 2, 1000}, {2, 3, 2000}});

        User sarah = upsertDevUser("sarah@example.com", "Sarah", "Benali", u -> {
            u.setXp(800);
            u.setLevel(5);
            u.setTotalXpEarned(4200);
            u.setCurrentLevelXpRequired(1000);
        });
        User youssef = upsertDevUser("youssef@example.com", "Youssef", "Amiri", u -> {
            u.setXp(500);
            u.setLevel(4);
            u.setTotalXpEarned(3500);
            u.setCurrentLevelXpRequired(1000);
        });
        upsertDevUser("fatima@example.com", "Fatima", "Zahra", u -> {
            u.setXp(900);
            u.setLevel(3);
            u.setTotalXpEarned(2900);
            u.setCurrentLevelXpRequired(1000);
        });
        upsertDevUser("omar@example.com", "Omar", "Idrissi", u -> {
            u.setXp(500);
            u.setLevel(2);
            u.setTotalXpEarned(1500);
            u.setCurrentLevelXpRequired(1000);
        });
        upsertDevUser("leila@example.com", "Leila", "Mansouri", u -> {
            u.setXp(200);
            u.setLevel(2);
            u.setTotalXpEarned(1200);
            u.setCurrentLevelXpRequired(1000);
        });

        seedRegisteredUserGamification(starterBadges);
        ensureSampleActivities(sarah, youssef);

        log.info("=== Dev login accounts (password for all: {}) ===", DEV_PASSWORD);
        log.info("  demo@spawnta.com       — main demo, gamification + activities");
        log.info("  sarah@example.com      — leaderboard");
        log.info("  youssef@example.com    — leaderboard");
        log.info("  fatima@example.com     — leaderboard");
        log.info("  omar@example.com       — leaderboard");
        log.info("  leila@example.com      — leaderboard");
        log.info("  (your signup account is unchanged — only XP/badges are topped up if present)");
        log.info("=== DevDataSeeder complete ===");
    }

    private List<Badge> ensureBadges() {
        Badge b1 = badgeRepository.findByName("Explorer").orElseGet(() ->
                badgeRepository.save(new Badge("Explorer", "Complete your first activity", null, 50)));
        Badge b2 = badgeRepository.findByName("Social Butterfly").orElseGet(() ->
                badgeRepository.save(new Badge("Social Butterfly", "Participate in 5 activities", null, 100)));
        Badge b3 = badgeRepository.findByName("Activity Master").orElseGet(() ->
                badgeRepository.save(new Badge("Activity Master", "Host 5 activities", null, 200)));
        badgeRepository.findByName("Reliable").orElseGet(() ->
                badgeRepository.save(new Badge("Reliable", "Perfect attendance for 10 activities", null, 150)));
        badgeRepository.findByName("Adventurer").orElseGet(() ->
                badgeRepository.save(new Badge("Adventurer", "Reach level 10", null, 300)));
        badgeRepository.findByName("Trailblazer").orElseGet(() ->
                badgeRepository.save(new Badge("Trailblazer", "Participate in 20 activities", null, 250)));
        badgeRepository.findByName("Community Leader").orElseGet(() ->
                badgeRepository.save(new Badge("Community Leader", "Host 15 activities", null, 400)));
        badgeRepository.findByName("Veteran").orElseGet(() ->
                badgeRepository.save(new Badge("Veteran", "Reach level 25", null, 500)));
        log.info("Badges catalog: {} entries", badgeRepository.count());
        return List.of(b1, b2, b3);
    }

    /**
     * Create or refresh a dev account: always verified, always password {@link #DEV_PASSWORD}.
     */
    private User upsertDevUser(String email, String firstName, String lastName, Consumer<User> extras) {
        User u = userRepository.findByEmail(email).orElseGet(User::new);
        boolean created = u.getId() == null;
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(DEV_PASSWORD));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(Role.USER);
        u.setEmailVerified(true);
        u.setVerificationToken(null);
        if (u.getXp() == null) {
            u.setXp(0);
        }
        if (u.getLevel() == null) {
            u.setLevel(1);
        }
        if (u.getTotalXpEarned() == null) {
            u.setTotalXpEarned(0);
        }
        if (u.getCurrentLevelXpRequired() == null) {
            u.setCurrentLevelXpRequired(1000);
        }
        extras.accept(u);
        User saved = userRepository.save(u);
        log.info("Dev user {} — {}", email, created ? "created" : "updated (password reset)");
        return saved;
    }

    private User upsertDevAdmin(String email, String firstName, String lastName) {
        User u = userRepository.findByEmail(email).orElseGet(User::new);
        boolean created = u.getId() == null;
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(DEV_PASSWORD));
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(Role.ADMIN);
        u.setEmailVerified(true);
        u.setVerificationToken(null);
        u.setSubscriptionTier("PROFESSIONAL");
        u.setBanned(false);
        u.setSuspendedUntil(null);
        u.setSuspensionReason(null);
        User saved = userRepository.save(u);
        log.info("Dev admin {} - {}", email, created ? "created" : "updated (password reset)");
        return saved;
    }

    private void awardStarterBadges(User user, List<Badge> badges) {
        for (Badge badge : badges) {
            if (!achievementRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                achievementRepository.save(new UserAchievement(user.getId(), badge.getId()));
            }
        }
    }

    private void ensureLevelHistory(User user, int[][] transitions) {
        if (!levelHistoryRepository.findByUserIdOrderByAchievedAtDesc(user.getId()).isEmpty()) {
            return;
        }
        for (int[] t : transitions) {
            saveLevelHistory(user, t[0], t[1], t[2]);
        }
    }

    private void seedRegisteredUserGamification(List<Badge> starterBadges) {
        userRepository.findByEmail("aoumhella98@gmail.com").ifPresent(u -> {
            if (u.getLevel() == null || u.getLevel() <= 1) {
                u.setXp(620);
                u.setLevel(4);
                u.setTotalXpEarned(3620);
                u.setCurrentLevelXpRequired(1000);
            }
            if (!u.isEmailVerified()) {
                u.setEmailVerified(true);
                u.setVerificationToken(null);
            }
            userRepository.save(u);
            awardStarterBadges(u, starterBadges);
            ensureLevelHistory(u, new int[][]{{1, 2, 1000}, {2, 3, 2000}, {3, 4, 3000}});
            log.info("Gamification synced for aoumhella98@gmail.com (password unchanged)");
        });
    }

    private void ensureSampleActivities(User sarah, User youssef) {
        if (activityRepository.findAll().stream().anyMatch(a -> "Randonnée au Toubkal".equals(a.getTitle()))) {
            log.info("Sample activities already present — skipping");
            return;
        }
        createSampleActivity("Randonnée au Toubkal", "Hiking & Trekking", "TRIP",
                sarah, 31.0600, -7.9150, 31.0610, -7.9160, LocalDateTime.now().plusDays(3));
        createSampleActivity("Coffee Meetup Casablanca", "Coffee & Cafes", "MEETUP",
                youssef, 33.5731, -7.5898, null, null, LocalDateTime.now().plusDays(1));
        createSampleActivity("Visite du Musée Mohammed VI", "Culture", "MEETUP",
                sarah, 33.9876, -6.8676, null, null, LocalDateTime.now().plusDays(5));
        createSampleActivity("Soirée Nightlife Marrakech", "Nightlife", "MEETUP",
                youssef, 31.6295, -7.9811, null, null, LocalDateTime.now().plusDays(2));
        createSampleActivity("Match Football Agadir", "Sports", "MEETUP",
                sarah, 30.4278, -9.5981, null, null, LocalDateTime.now().plusDays(7));
        log.info("Created 5 sample activities for recommendations/map");
    }

    private void saveLevelHistory(User user, int oldLevel, int newLevel, int xpAtTime) {
        levelHistoryRepository.save(new UserLevelHistory(user, oldLevel, newLevel, xpAtTime));
    }

    private void createSampleActivity(String title, String category, String type,
                                      User host, double lat, double lng,
                                      Double destLat, Double destLng,
                                      LocalDateTime scheduledAt) {
        Activity a = new Activity();
        a.setTitle(title);
        a.setDescription("Activité de démonstration pour tester les recommandations Spawnta.");
        a.setCategory(category);
        a.setActivityType(ActivityType.valueOf(type));
        a.setHost(host);
        a.setScheduledAt(scheduledAt);
        a.setParticipationMode(ParticipationMode.DIRECT);
        a.setMaxParticipants(20);

        if ("TRIP".equals(type)) {
            a.setStartLocation(geometryFactory.createPoint(new Coordinate(lng, lat)));
            if (destLat != null) {
                a.setDestination(geometryFactory.createPoint(new Coordinate(destLng, destLat)));
            }
        } else {
            a.setLocation(geometryFactory.createPoint(new Coordinate(lng, lat)));
        }

        activityRepository.save(a);
    }
}
