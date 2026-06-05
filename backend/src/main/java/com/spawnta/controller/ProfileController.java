package com.spawnta.controller;

import com.spawnta.dto.UpdateInterestsRequest;
import com.spawnta.dto.UpdateProfileRequest;
import com.spawnta.dto.UserProfileResponse;
import com.spawnta.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(
        @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(profileService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
        @AuthenticationPrincipal String email,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(email, request));
    }

    @PutMapping("/interests")
    public ResponseEntity<UserProfileResponse> updateInterests(
        @AuthenticationPrincipal String email,
        @Valid @RequestBody UpdateInterestsRequest request
    ) {
        return ResponseEntity.ok(profileService.updateInterests(email, request));
    }

    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileResponse> uploadAvatar(
        @AuthenticationPrincipal String email,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(profileService.uploadAvatar(email, file));
    }

    @PostMapping(value = "/gallery", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileResponse> addGalleryPhoto(
        @AuthenticationPrincipal String email,
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(profileService.addGalleryPhoto(email, file));
    }

    @DeleteMapping("/gallery")
    public ResponseEntity<UserProfileResponse> removeGalleryPhoto(
        @AuthenticationPrincipal String email,
        @RequestParam("url") String photoUrl
    ) {
        return ResponseEntity.ok(profileService.removeGalleryPhoto(email, photoUrl));
    }
}
