package com.example.demo.mq;

public interface TaskMessagePublisher {

    void publish(RocketMessage<?> message) throws Exception;
}
