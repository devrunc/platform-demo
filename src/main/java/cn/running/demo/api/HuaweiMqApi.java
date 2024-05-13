package cn.running.demo.api;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.running.demo.common.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 华为MQ对接：Kafka、RocketMQ
 *
 * @Author hao cheng
 * @Date 2023/3/15 14:30
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/mq")
public class HuaweiMqApi {

    /**
     * KafkaTemplate
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * RocketMQTemplate
     */
    private final RocketMQTemplate rocketMQTemplate;


    /**
     * 监听获取kafka消息
     */
    @KafkaListener(id = CommonConstant.DEFAULT_GROUP_NAME, topics = CommonConstant.DEFAULT_TOPIC_NAME)
    public void listenerKafkaMsg(String msg){
        log.info("监听到Kafka消息：{}", msg);
    }

    /**
     * 向kafka中塞入消息
     *
     * @param msg 消息
     */
    @GetMapping("/setKafkaMsg")
    public void setKafkaMsg(@RequestParam("msg") String msg){
        // send message
        kafkaTemplate.send(CommonConstant.DEFAULT_TOPIC_NAME, msg);
    }

    /**
     * 批量向kafka中塞入消息
     */
    @GetMapping("/batchKafkaMsg")
    public void batchKafkaMsg() {
        for (int i = 0; i < 10; i++) {
            // 生成随机字符串
            String msg = RandomUtil.randomString(1067200);
            // send message
            kafkaTemplate.send(CommonConstant.DEFAULT_TOPIC_NAME, msg).addCallback(success -> {
                // 服务端返回的元数据
                RecordMetadata metadata = success.getRecordMetadata();
                log.info("消息塞入成功！topic：{}，partition：{}，offset：{}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }, fail -> {
                log.error("消息塞入失败：{}！", fail.getMessage());
            });
        }
    }

    /**
     * 批量向rocketmq中塞入消息
     */
    @GetMapping("/batchRocketmqMsg")
    public void batchRocketmqMsg() {
        for (int i = 0; i < 1000; i++) {
            // 生成随机字符串
            String randomStr = RandomUtil.randomString(6);
            // 异步send message
            rocketMQTemplate.asyncSend(StrUtil.format("{}:{}", CommonConstant.DEFAULT_TOPIC_NAME, "test_use1"), randomStr, new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    log.info("消息塞入成功！msgId：{}，sendStatus：{}，regionId：{}，queueOffset：{}",
                            result.getMsgId(), result.getSendStatus(), result.getRegionId(), result.getQueueOffset());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("消息塞入失败：{}！", e.getMessage());
                }
            });
        }
    }

}
