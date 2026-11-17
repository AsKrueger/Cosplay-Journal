package com.cosplayjournal.infrastructure.config;

import com.cosplayjournal.infrastructure.adapter.out.storage.LocalDiskPhotoStorageAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las fotos guardadas en disco bajo /uploads/. */
@Configuration
public class PhotoStorageWebConfiguration implements WebMvcConfigurer {

    private final LocalDiskPhotoStorageAdapter storage;

    public PhotoStorageWebConfiguration(LocalDiskPhotoStorageAdapter storage) {
        this.storage = storage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(storage.root().toUri().toString() + "/");
    }
}
