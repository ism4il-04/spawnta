package com.spawnta.service;

import com.spawnta.dto.UpdateInterestsRequest;
import com.spawnta.dto.UpdateProfileRequest;
import com.spawnta.dto.UserProfileResponse;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ProfileService(UserRepository userRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public UserProfileResponse getProfile(String email) {
        User user = findUser(email);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, @Valid UpdateProfileRequest request) {
        User user = findUser(email);
        user.setBio(request.bio());
        user.setFacebook(request.facebook());
        user.setInstagram(request.instagram());
        user.setWhatsapp(request.whatsapp());
        user.setVisitedCountries(request.visitedCountries());
        user.setProfilePublic(request.profilePublic());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse updateInterests(String email, @Valid UpdateInterestsRequest request) {
        if (request.interests() != null && request.interests().size() > 10) {
            throw new IllegalArgumentException("You can select at most 10 interests");
        }
        User user = findUser(email);
        user.setInterests(request.interests());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse uploadAvatar(String email, MultipartFile file) throws IOException {
        log.info("[Avatar] Upload started for user: {}, file: {}, size: {} bytes", email, file.getOriginalFilename(), file.getSize());
        try {
            User user = findUser(email);
            String url = cloudinaryService.uploadAvatar(file, email);
            log.info("[Avatar] Upload success for user: {}, url: {}", email, url);
            user.setAvatarUrl(url);
            return toResponse(userRepository.save(user));
        } catch (IOException e) {
            log.error("[Avatar] Cloudinary upload FAILED for user: {} — {}", email, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[Avatar] Unexpected error for user: {} — {}", email, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public UserProfileResponse addGalleryPhoto(String email, MultipartFile file) throws IOException {
        User user = findUser(email);
        String url = cloudinaryService.uploadGalleryPhoto(file, email);
        user.getGallery().add(url);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse removeGalleryPhoto(String email, String photoUrl) {
        User user = findUser(email);
        user.getGallery().remove(photoUrl);
        return toResponse(userRepository.save(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getInterests(),
            user.getGallery(),
            user.getVisitedCountries(),
            user.getFacebook(),
            user.getInstagram(),
            user.getWhatsapp(),
            user.isProfilePublic(),
            user.getCreatedAt()
        );
    }
}
