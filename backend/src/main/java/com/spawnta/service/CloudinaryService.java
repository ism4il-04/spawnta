package com.spawnta.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
        @Value("${cloudinary.cloud-name}") String cloudName,
        @Value("${cloudinary.api-key}") String apiKey,
        @Value("${cloudinary.api-secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key",    apiKey,
            "api_secret", apiSecret,
            "secure",     true
        ));
    }

    /**
     * Uploads an image and returns its secure URL.
     * Applies eager transformation to resize to 400×400 fill crop for avatars.
     */
    public String uploadAvatar(MultipartFile file, String userEmail) throws IOException {
        String publicId = "avatars/" + sanitize(userEmail);
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "public_id",   publicId,
                "overwrite",   true,
                "resource_type", "image",
                "eager", Arrays.asList(new Transformation().width(400).height(400).crop("fill").quality("auto"))
            )
        );
        return (String) result.get("secure_url");
    }

    /**
     * Uploads a gallery photo and returns its secure URL.
     */
    public String uploadGalleryPhoto(MultipartFile file, String userEmail) throws IOException {
        String publicId = "gallery/" + sanitize(userEmail) + "/" + System.currentTimeMillis();
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "public_id",     publicId,
                "resource_type", "image",
                "eager", Arrays.asList(new Transformation().width(1200).quality("auto:good"))
            )
        );
        return (String) result.get("secure_url");
    }

    /**
     * Deletes an asset by its public ID derived from a Cloudinary URL.
     */
    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}
