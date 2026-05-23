package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(unique = true)
    private String username;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String city;

    @Column(nullable = false)
    private int level = 1;

    @Column(nullable = false)
    private int xp = 0;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "is_premium", nullable = false)
    private boolean isPremium = false;

    @Column(name = "is_banned", nullable = false)
    private boolean isBanned = false;

    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;

    @Column(length = 300)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public User() {}

    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ───────────────────────── LIFECYCLE ─────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ───────────────────────── GETTERS / SETTERS ─────────────────────────

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

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }

    public void setXp(int xp) { this.xp = xp; }

    public String getProfilePictureUrl() { return profilePictureUrl; }

    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    public boolean isPremium() { return isPremium; }

    public void setPremium(boolean premium) { isPremium = premium; }

    public boolean isBanned() { return isBanned; }

    public void setBanned(boolean banned) { isBanned = banned; }

    public boolean isEmailVerified() { return isEmailVerified; }

    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public String getBio() { return bio; }

    public void setBio(String bio) { this.bio = bio; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}