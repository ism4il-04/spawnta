package com.spawnta.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class MessageParser {

    private static final int MAX_LENGTH = 2000;

    // Simple pattern to detect suspicious script tags, HTML event handlers, javascript protocol, or SQL comments
    private static final Pattern MALICIOUS_PATTERNS = Pattern.compile(
        "(?i)<script.*?>.*?</script>|href\\s*=\\s*[\"']\\s*javascript:|on\\w+\\s*=\\s*[\"'].*?[\"']|eval\\s*\\(|union\\s+select|'\\s*or\\s+'1'\\s*=\\s*'1"
    );

    // Demonstration blacklist for inappropriate content (Requirement 4.2)
    private static final Set<String> INAPPROPRIATE_WORDS = Set.of(
        "insulte1", "insulte2", "spamlink", "phishingcontent"
    );

    public String parseAndSanitize(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String trimmed = content.trim();

        // 1. Limit length to 2000 characters
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Message exceeds the limit of " + MAX_LENGTH + " characters");
        }

        // 2. Scan for malicious injection attempts (XSS, SQLi)
        if (MALICIOUS_PATTERNS.matcher(trimmed).find()) {
            throw new IllegalArgumentException("Malicious code or script injection attempt detected");
        }

        // 3. Scan for inappropriate content
        String lowerCaseContent = trimmed.toLowerCase();
        for (String badWord : INAPPROPRIATE_WORDS) {
            if (lowerCaseContent.contains(badWord)) {
                throw new IllegalArgumentException("Message contains inappropriate or forbidden content");
            }
        }

        // 4. Escape HTML tags to guarantee no XSS is rendered
        return HtmlUtils.htmlEscape(trimmed);
    }
}
