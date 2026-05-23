package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Rich Profile Fields ───────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** Up to 10 user-selected interests stored as a collection. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    @Enumerated(EnumType.STRING)
    private Set<Interest> interests = new HashSet<>();

    /** Cloudinary photo URLs for the personal gallery. */
    @ElementCollection
    @CollectionTable(name = "user_gallery", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "photo_url", length = 500)
    private List<String> gallery = new ArrayList<>();

    /** ISO-3166 alpha-2 country codes. */
    @ElementCollection
    @CollectionTable(name = "user_visited_countries", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "country_code", length = 3)
    private List<String> visitedCountries = new ArrayList<>();

    // ── Social Network Links ──────────────────────────────────────────────────

    @Column(length = 255)
    private String facebook;

    @Column(length = 255)
    private String instagram;

    @Column(length = 50)
    private String whatsapp;

    // ── Privacy ───────────────────────────────────────────────────────────────

    /** When false, bio/gallery/social links are hidden from other users. */
    @Column(name = "profile_public", nullable = false)
    private boolean profilePublic = true;

    // ── Email Verification ────────────────────────────────────────────────────

    @Column(name = "email_verified", columnDefinition = "boolean not null default false")
    private boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ── Getters & Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Set<Interest> getInterests() { return interests; }
    public void setInterests(Set<Interest> interests) { this.interests = interests; }

    public List<String> getGallery() { return gallery; }
    public void setGallery(List<String> gallery) { this.gallery = gallery; }

    public List<String> getVisitedCountries() { return visitedCountries; }
    public void setVisitedCountries(List<String> visitedCountries) { this.visitedCountries = visitedCountries; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }

    public boolean isProfilePublic() { return profilePublic; }
    public void setProfilePublic(boolean profilePublic) { this.profilePublic = profilePublic; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
}

