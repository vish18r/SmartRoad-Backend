package com.smartroad.services.core.service.storage;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded work-photo files on local disk under a per-project directory and
 * returns the URL path clients use to fetch them back from {@code /uploads/**}.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final Path baseDir;

    /**
     * Constructs the service and resolves the configured base directory to an absolute path.
     *
     * @param workPhotosDir the configured base directory for stored work photos
     */
    public FileStorageService(@Value("${smartroad.storage.work-photos-dir:uploads/work-photos}") String workPhotosDir) {
        this.baseDir = Path.of(workPhotosDir).toAbsolutePath();
    }

    /**
     * Returns the absolute base directory work photos are stored under.
     *
     * @return the absolute base directory
     */
    public Path getBaseDir() {
        return baseDir;
    }

    /**
     * Validates, stores, and returns the servable URL for an uploaded work-photo file.
     *
     * @param projectId the project UUID the photo belongs to
     * @param file the uploaded file
     * @return the URL path the file is servable from, e.g. {@code /uploads/work-photos/{projectId}/{filename}}
     * @throws SmartRoadException if the file is empty, not an accepted image type, too large, or
     *                            cannot be written to disk
     */
    public String store(UUID projectId, MultipartFile file) throws SmartRoadException {
        if (file == null || file.isEmpty()) {
            throw invalid("photo.upload.empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw invalid("photo.upload.too.large");
        }

        String extension = resolveExtension(file.getContentType());
        String filename = UUID.randomUUID() + "." + extension;
        Path projectDir = baseDir.resolve(projectId.toString());

        try {
            Files.createDirectories(projectDir);
            Path target = projectDir.resolve(filename);
            file.transferTo(target);
        } catch (IOException e) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                    "photo.upload.failed", e);
        }

        return "/uploads/work-photos/" + projectId + "/" + filename;
    }

    /**
     * Resolves a safe file extension from the upload's content type, rejecting any type not on
     * the accepted-image whitelist.
     *
     * @param contentType the uploaded file's declared content type
     * @return the whitelisted file extension to use
     * @throws SmartRoadException if the content type is missing or not an accepted image type
     */
    private String resolveExtension(String contentType) throws SmartRoadException {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw invalid("photo.upload.invalid.type");
        }
        return EXTENSION_BY_CONTENT_TYPE.get(contentType);
    }

    /**
     * Builds a bad-request exception for the given message key.
     *
     * @param messageKey the message key to report
     * @return the {@link SmartRoadException} to throw
     */
    private SmartRoadException invalid(String messageKey) {
        return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_INVALID_INPUT, messageKey);
    }
}
