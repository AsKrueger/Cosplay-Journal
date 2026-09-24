package com.cosplayjournal.infrastructure.config;

import com.cosplayjournal.application.port.out.CosplayRepositoryPort;
import com.cosplayjournal.application.service.CosplayApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CosplayApplicationService cosplayApplicationService(CosplayRepositoryPort cosplayRepositoryPort) {
        return new CosplayApplicationService(cosplayRepositoryPort);
    }
}
