package com.ck.hackaton.artreid_3.artreid3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.Properties;

@Slf4j
@Component
@EnableScheduling
public class SlaConfigReloader {

    private final SlaConfig slaConfig;
    private long lastModifiedTime = 0;

    public SlaConfigReloader(SlaConfig slaConfig) {
        this.slaConfig = slaConfig;
    }

    @PostConstruct
    public void init() {
        checkForUpdates();
    }

    @Scheduled(fixedDelay = 5000)
    public void checkForUpdates() {
        try {
            File file = new File("sla-config.yml");
            if (!file.exists()) {
                file = new File("src/main/resources/sla-config.yml");
                if (!file.exists()) return;
            }

            long currentModifiedTime = file.lastModified();
            if (currentModifiedTime > lastModifiedTime) {
                lastModifiedTime = currentModifiedTime;
                reloadConfig(file);
            }
        } catch (Exception e) {
            log.error("Failed to check/reload SlaConfig", e);
        }
    }

    private void reloadConfig(File file) {
        log.info("Reloading config from {}", file.getAbsolutePath());
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new FileSystemResource(file));
        Properties properties = factory.getObject();

        if (properties != null) {
            ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
            Binder binder = new Binder(source);
            binder.bind("sla", Bindable.ofInstance(slaConfig));
            log.info("Successfully reloaded SlaConfig");
        }
    }
}
