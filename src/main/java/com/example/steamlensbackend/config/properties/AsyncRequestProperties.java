package com.example.steamlensbackend.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "async.request")
public record AsyncRequestProperties(
        RedisProperties redis,
        KafkaProperties kafka
) {
    public record RedisProperties(
            String keyPrefix,
            int ttlHours
    ) {
        public RedisProperties {
            if (keyPrefix == null) {
                keyPrefix = "async:request:";
            }
            if (ttlHours <= 0) {
                ttlHours = 1;
            }
        }
    }

    public record KafkaProperties(
            String topic
    ) {
        public KafkaProperties {
            if (topic == null) {
                topic = "shared-library-requests";
            }
        }
    }
}
