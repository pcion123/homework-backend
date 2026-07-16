package com.example.demo.mq;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public record RocketMessage<T>(Meta meta, T payload) {

    public RocketMessage {
        Objects.requireNonNull(meta, "meta must not be null");
        Objects.requireNonNull(payload, "payload must not be null");
    }

    public static <T> RocketMessage<T> of(T payload) {
        return of(payload, null, null, Clock.systemUTC());
    }

    public static <T> RocketMessage<T> of(T payload, String key) {
        return of(payload, key, null, Clock.systemUTC());
    }

    public static <T> RocketMessage<T> of(T payload, String key, String traceId) {
        return of(payload, key, traceId, Clock.systemUTC());
    }

    public static <T> RocketMessage<T> of(T payload, String key, String traceId, Clock clock) {
        Objects.requireNonNull(clock, "clock must not be null");

        String messageId = UUID.randomUUID().toString();
        Meta meta = new Meta(messageId, key, traceId, null, null, clock.millis(), null);

        return of(payload, meta);
    }

    public static <T> RocketMessage<T> of(T payload, Meta meta) {
        return new RocketMessage<>(meta, payload);
    }

    public RocketMessage<T> withSource(String sourceService, String sourceIp, String version) {
        return new RocketMessage<>(meta.withSource(sourceService, sourceIp, version), payload);
    }

    public record Meta(String messageId, String key, String traceId, String sourceService,
            String sourceIp, long timestamp, String version) {

        public Meta {
            messageId = requireText(messageId, "messageId");
            key = defaultText(key, messageId, "key");
            traceId = defaultText(traceId, messageId, "traceId");
            sourceService = optionalText(sourceService, "sourceService");
            sourceIp = optionalText(sourceIp, "sourceIp");
            version = optionalText(version, "version");
        }

        public Meta withSource(String sourceService, String sourceIp, String version) {
            return new Meta(messageId, key, traceId, sourceService, sourceIp, timestamp, version);
        }

        public Meta(String messageId, String traceId, String sourceService, String sourceIp,
                long timestamp, String version) {
            this(messageId, messageId, traceId, sourceService, sourceIp, timestamp, version);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    private static String defaultText(String value, String defaultValue, String fieldName) {
        if (value == null) {
            return defaultValue;
        }

        return requireText(value, fieldName);
    }

    private static String optionalText(String value, String fieldName) {
        if (value == null) {
            return null;
        }

        return requireText(value, fieldName);
    }

}
