package com.smartroad.services.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Registers the static resource handler that serves uploaded work-photo files from
 * {@code /uploads/**}. The underlying directory is left behind the default
 * authenticated-by-default security rule, since the files may hold sensitive project photos.
 *
 * @author Vishal
 * @version 1.0
 */
@Configuration
public class StorageWebConfig implements WebMvcConfigurer {

    private final String workPhotosDir;

    /**
     * Constructs the configuration with the configured work-photos base directory.
     *
     * @param workPhotosDir the configured base directory for stored work photos
     */
    public StorageWebConfig(@Value("${smartroad.storage.work-photos-dir:uploads/work-photos}") String workPhotosDir) {
        this.workPhotosDir = workPhotosDir;
    }

    /**
     * Maps {@code /uploads/work-photos/**} to the absolute directory work photos are stored under.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path baseDir = Path.of(workPhotosDir).toAbsolutePath();
        registry.addResourceHandler("/uploads/work-photos/**")
                .addResourceLocations("file:" + baseDir + "/");
    }
}
