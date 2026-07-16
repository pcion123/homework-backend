package com.example.demo.model;

import java.time.LocalDateTime;

public record TaskJob(
        String id,
        String status,
        String payload,
        LocalDateTime executeTime) {
}