package com.heima.file.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinIOConfig {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endPoint;
    private String readPath;

    @Bean
    @ConditionalOnProperty(prefix = "minio", name = "endpoint")
    public MinioClient minioClient() {
        System.out.println("====================minIOConfig=================:"+this);
        return MinioClient.builder()
            .endpoint(endPoint)
            .credentials(accessKey, secretKey)
            .build();
    }
}