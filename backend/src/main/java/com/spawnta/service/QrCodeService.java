package com.spawnta.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
public class QrCodeService {

    @Value("${spring.app.jwt.secret:spawnta_super_secret_jwt_key_2024_make_it_very_long_to_satisfy_security_requirements_min_32_chars}")
    private String secret;

    public String toPngDataUri(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR code", e);
        }
    }

    public String generateSignedToken(Long activityId, long expiryTimestamp) {
        String payload = activityId + ":" + expiryTimestamp;
        String signature = sign(payload);
        return payload + ":" + signature;
    }

    public boolean validateSignedToken(String token, Long expectedActivityId) {
        if (token == null) return false;
        String[] parts = token.split(":");
        if (parts.length != 3) return false;

        try {
            Long activityId = Long.parseLong(parts[0]);
            long expiryTimestamp = Long.parseLong(parts[1]);
            String signature = parts[2];

            if (!activityId.equals(expectedActivityId)) return false;
            if (System.currentTimeMillis() > expiryTimestamp) return false;

            String payload = activityId + ":" + expiryTimestamp;
            String expectedSignature = sign(payload);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String sign(String data) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            byte[] hash = sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC signing failed", e);
        }
    }
}
