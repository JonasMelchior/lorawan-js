package org.cibicom.iot.js.ui.config.gprc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    @Value("${cs.api.host}")
    private String apiHost;

    @Value("${cs.api.port}")
    private int apiPort;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder
                .forAddress(apiHost, apiPort)
                .usePlaintext()  // only if your server is not using TLS
                .build();
    }
}