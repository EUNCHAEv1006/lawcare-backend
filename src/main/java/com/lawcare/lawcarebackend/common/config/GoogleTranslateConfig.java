package com.lawcare.lawcarebackend.common.config;

import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleTranslateConfig {

    @Value("${google.translate.apiKey}")
    private String googleApiKey;

    @Bean
    public Translate translate() {
        TranslateOptions options = TranslateOptions.newBuilder()
                                                   .setHeaderProvider(FixedHeaderProvider.create("X-Goog-Api-Key", googleApiKey))
                                                   .build();

        return options.getService();
    }
}
