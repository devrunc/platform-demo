package cn.running.demo.components;

import cn.running.demo.common.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 *
 * RocketMQ消费监听组件
 *
 * @Author hao cheng
 * @Date 2023/5/15 15:30
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = CommonConstant.DEFAULT_TOPIC_NAME,
        consumerGroup = CommonConstant.DEFAULT_GROUP_NAME,
        // 异步消费模式
        consumeMode = ConsumeMode.CONCURRENTLY)
public class RocketMqConsumerComponent implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("监听到RocketMQ消息：{}", message);
    }
}
