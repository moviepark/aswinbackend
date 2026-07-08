package com.vaultflow.orchestrator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FrontendConfig implements WebMvcConfigurer {

    private String getFrontendPath() {
        Path fromProjectRoot = Paths.get("frontend").toAbsolutePath().normalize();
        Path fromBackendFolder = Paths.get("../frontend").toAbsolutePath().normalize();

        Path selectedPath = Files.exists(fromProjectRoot)
                ? fromProjectRoot
                : fromBackendFolder;

        return selectedPath.toUri().toString();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(getFrontendPath())
                .setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/")
                .setViewName("forward:/index.html");
    }
}