package com.example.demo.mq;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.demo.util.JsonUtil;

@Component
public class RocketMqTaskMessagePublisher
        implements TaskMessagePublisher, InitializingBean, DisposableBean {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RocketMqTaskMessagePublisher.class);

    private final DefaultMQProducer producer;
    private final String topic;
    private final JsonUtil jsonUtil;
    private final String sourceService;
    private final String sourceIp;
    private final String version;

    public RocketMqTaskMessagePublisher(JsonUtil jsonUtil,
            @Value("${rocketmq.producer.group:task-schedule-producer-group}") String producerGroup,
            @Value("${rocketmq.name-server:127.0.0.1:9876}") String nameServer,
            @Value("${rocketmq.task-topic:task-schedule-topic}") String topic,
            @Value("${rocketmq.create-topic-key:TBW102}") String createTopicKey,
            @Value("${rocketmq.producer.send-timeout:10000}") int sendTimeout,
            @Value("${rocketmq.producer.vip-channel-enabled:false}") boolean vipChannelEnabled,
            @Value("${spring.application.name:demo}") String sourceService,
            @Value("${rocketmq.message.source-ip:}") String sourceIp,
            @Value("${rocketmq.message.version:1.0}") String version) {
        this.producer = new DefaultMQProducer(producerGroup);
        this.producer.setNamesrvAddr(nameServer);
        this.producer.setCreateTopicKey(createTopicKey);
        this.producer.setSendMsgTimeout(sendTimeout);
        this.producer.setVipChannelEnabled(vipChannelEnabled);
        this.topic = topic;
        this.jsonUtil = jsonUtil;
        this.sourceService = sourceService;
        this.sourceIp = normalizeSourceIp(sourceIp);
        this.version = version;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        producer.start();
        LOGGER.info(
                "RocketMQ producer started. group={}, namesrv={}, topic={}, createTopicKey={}, vipChannelEnabled={}, sendTimeout={}ms",
                producer.getProducerGroup(), producer.getNamesrvAddr(), topic,
                producer.getCreateTopicKey(), producer.isVipChannelEnabled(),
                producer.getSendMsgTimeout());
    }

    @Override
    public void publish(RocketMessage<?> message) throws Exception {
        RocketMessage<?> publishMessage = toPublishMessage(message);
        Message mqMessage = new Message(topic,
                jsonUtil.toJson(publishMessage).getBytes(StandardCharsets.UTF_8));
        mqMessage.setKeys(publishMessage.meta().key());
        mqMessage.putUserProperty("messageId", publishMessage.meta().messageId());
        mqMessage.putUserProperty("key", publishMessage.meta().key());
        mqMessage.putUserProperty("traceId", publishMessage.meta().traceId());
        mqMessage.putUserProperty("timestamp", String.valueOf(publishMessage.meta().timestamp()));

        SendResult sendResult = producer.send(mqMessage);
        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
            throw new IllegalStateException(
                    "RocketMQ send failed. status=" + sendResult.getSendStatus());
        }

        LOGGER.info(
                "Published message to RocketMQ. messageId={}, traceId={}, topic={}, sendStatus={}, msgId={}",
                publishMessage.meta().messageId(), publishMessage.meta().traceId(), topic,
                sendResult.getSendStatus(), sendResult.getMsgId());
    }

    @Override
    public void destroy() {
        producer.shutdown();
    }

    RocketMessage<?> toPublishMessage(RocketMessage<?> message) {
        return message.withSource(sourceService, sourceIp, version);
    }

    private static String normalizeSourceIp(String sourceIp) {
        if (sourceIp != null && !sourceIp.isBlank()) {
            return sourceIp;
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException exception) {
            return "unknown";
        }
    }
}
