package com.spawnta.subscription.service;

import com.spawnta.entity.Badge;
import com.spawnta.entity.User;
import com.spawnta.service.BadgeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubscriptionDiscountService {

    private final BadgeService badgeService;

    public SubscriptionDiscountService(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    public int calculateTotalDiscountPercentage(User user) {
        int discount = 0;

        // 1. Level-based discounts
        int level = user.getLevel();
        if (level >= 20) {
            discount += 30;
        } else if (level >= 10) {
            discount += 15;
        } else if (level >= 5) {
            discount += 5;
        }

        // 2. Badge-based boosters
        List<Badge> userBadges = badgeService.getAllUserAchievements(user.getId());
        Set<String> badgeNames = userBadges.stream()
                .map(Badge::getName)
                .collect(Collectors.toSet());

        if (badgeNames.contains("Community Leader")) {
            discount += 10;
        }
        if (badgeNames.contains("Veteran")) {
            discount += 10;
        }

        // Max total discount cap at 50%
        return Math.min(discount, 50);
    }

    public String getDiscountReason(User user) {
        StringBuilder reason = new StringBuilder();
        int level = user.getLevel();
        if (level >= 5) {
            reason.append("Réduction de niveau (").append(level >= 20 ? "30%" : (level >= 10 ? "15%" : "5%")).append(")");
        }

        List<Badge> userBadges = badgeService.getAllUserAchievements(user.getId());
        Set<String> badgeNames = userBadges.stream()
                .map(Badge::getName)
                .collect(Collectors.toSet());

        if (badgeNames.contains("Community Leader")) {
            if (reason.length() > 0) reason.append(" + ");
            reason.append("Bonus Community Leader (10%)");
        }
        if (badgeNames.contains("Veteran")) {
            if (reason.length() > 0) reason.append(" + ");
            reason.append("Bonus Veteran (10%)");
        }

        if (reason.length() == 0) return null;
        return reason.toString();
    }
}
