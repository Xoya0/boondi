package com.boondi.infrastructure.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Confirms a MultipartFile's bytes actually match its claimed image/* content type, rather
 * than trusting the client-supplied Content-Type header alone. Without this, a caller could
 * label an arbitrary payload "image/jpeg" and have it stored and served back (from MinIO)
 * under that same trusted content type.
 */
@Component
public class ImageContentValidator {

    private static final int HEADER_BYTES = 12;

    public boolean matches(MultipartFile file, String claimedContentType) {
        byte[] header = readHeader(file);
        return switch (claimedContentType) {
            case "image/jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/webp" -> startsWith(header, 0x52, 0x49, 0x46, 0x46) // "RIFF"
                    && header.length >= 12
                    && header[8] == 0x57 && header[9] == 0x45 && header[10] == 0x42 && header[11] == 0x50; // "WEBP"
            default -> false;
        };
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] buffer = new byte[HEADER_BYTES];
            int totalRead = 0;
            int read;
            while (totalRead < HEADER_BYTES && (read = in.read(buffer, totalRead, HEADER_BYTES - totalRead)) != -1) {
                totalRead += read;
            }
            return totalRead == HEADER_BYTES ? buffer : new byte[0];
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private boolean startsWith(byte[] header, int... expected) {
        if (header.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((header[i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
