package cn.running.demo.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.*;
import cn.hutool.crypto.SecureUtil;
import cn.running.demo.common.CommonConstant;
import cn.zhxu.okhttps.HTTP;
import cn.zhxu.okhttps.jackson.JacksonMsgConvertor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 华为ROMA对接：APIC、MQS
 *
 * @Author hao cheng
 * @Date 2024/5/24 10:31
 */
@Slf4j
public class HuaweiRomaApi {

    /**
     * 华为APIC对接
     */
    @RestController
    @AllArgsConstructor
    @RequestMapping("/apic")
    public static class APIC {

        /**
         * 构建请求
         */
        private final HTTP request = HTTP.builder()
                // 配置
                .config(builder -> {
                    // 配置连接池
                    builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
                    // 连接超时时间
                    builder.connectTimeout(10, TimeUnit.SECONDS);
                    // 写入超时时间
                    builder.writeTimeout(3, TimeUnit.SECONDS);
                    // 读取超时时间
                    builder.readTimeout(5, TimeUnit.SECONDS);
                    // 设置重试机制
                    builder.addInterceptor(chain -> {
                        int retryTimes = 0;
                        for (; ; ) {
                            try {
                                return chain.proceed(chain.request());
                            } catch (Exception e) {
                                if (retryTimes >= 3) throw e;
                                retryTimes++;
                            }
                        }
                    });
                })
                // 消息序列化转换器
                .addMsgConvertor(new JacksonMsgConvertor()).build();


        /**
         * 请求apic地址（无认证）
         *
         * @param appKey api关联的集成应用appKey
         */
        @GetMapping("/requestApic1")
        public String requestApic1(@RequestParam("appKey") String appKey) {
            return request.sync("https://127.0.0.1/test")
                    // 请求头
                    .addHeader("X-HW-ID", appKey)
                    .addUrlPara(new HashMap<String, Object>() {{
                        // 请求传递参数
                        put("id", 123);
                        put("name", "running");
                    }})
                    .get()
                    .getBody().toBean(String.class);
        }

        /**
         * 请求apic地址（APP认证）
         */
        @GetMapping("/requestApic2")
        public String requestApic2() {
            String url = "https://127.0.0.1/test";
            String appKey = "3faf5278-dabc-40a3-ad62-68c8b61c1129";
            String appKSecret = "um5+7BP34V=q#6@z/baA-ik/!sVt-eJ72$%.fV.W7u9#Inq07.wqk02A0Ws.cO$R";
            String algorithm = "SDK-HMAC-SHA256";
            String utc = getNowUTCStr();
            // 请求参数
            HashMap<String, Object> paramsMap = new HashMap<String, Object>() {{
                // 请求传递参数
                put("id", 123);
                put("name", "running");
            }};
            // 头部参数
            HashMap<String, Object> headerMap = new HashMap<String, Object>() {{
                // 请求头参数
                put("x-sdk-date", utc);
                put("age", 24);
            }};
            // 请求体
            String bodyStr = "{\"test1\": \"test11\"}";
            // 规范请求
            HashMap<String, String> canonicalRequest = new HashMap<String, String>() {{
                // HTTP请求方法
                put("HTTPRequestMethod", HTTP.GET);
                // URI
                put("CanonicalURI", URLUtil.getPath(url) + CharPool.SLASH);
                // 请求参数字符串
                put("CanonicalQueryString", MapUtil.sortJoin(paramsMap, "&", "=", Boolean.FALSE));
                // 头部参数字符串
                put("CanonicalHeaders", MapUtil.sortJoin(headerMap, StrPool.LF, StrPool.COLON, Boolean.FALSE) + StrPool.COLON);
                // 参与签名声明
                put("SignedHeaders", "x-sdk-date");
                // 请求消息体，创建哈希值
                put("RequestPayload", SecureUtil.sha256(bodyStr));
            }};
            StrBuilder canonicalRequestStr = StrUtil.strBuilder();
            canonicalRequest.forEach((k, v) -> canonicalRequestStr.append(v).append(StrPool.LF));
            // 待签字符串
            String signStr = algorithm.concat(StrPool.LF).concat(utc).concat(StrPool.LF).concat(canonicalRequestStr.toStringAndReset().trim());
            System.out.println(StrUtil.format("{} Access={}, SignedHeaders={}, Signature={}",
                            algorithm, appKey, canonicalRequest.get("SignedHeaders"),
                    SecureUtil.hmacSha256(appKSecret).digestHex(signStr)));
            return request.sync(url)
                    // 请求头
                    .addHeader("X-HW-ID", appKey)
                    .addHeader("Authorization: ", StrUtil.format("{} Access={}, SignedHeaders={}, Signature={}",
                            algorithm, appKey, canonicalRequest.get("SignedHeaders"),
                            SecureUtil.hmacSha256(appKSecret).digestHex(signStr)))
                    .post()
                    .getBody().toBean(String.class);
        }

        /**
         * 获取当前UTC时间
         *
         * @return utc时间
         */
        private static String getNowUTCStr() {
            // 格式化当前UTC时间
            return DateUtil.format(DateUtil.date(), "yyyyMMdd'T'HHmmss'Z'");
        }

    }

    /**
     * 华为MQ对接：Kafka、RocketMQ
     */
    @RestController
    @AllArgsConstructor
    @RequestMapping("/mqs")
    public static class MQS {

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
        public void listenerKafkaMsg(String msg) {
            log.info("监听到Kafka消息：{}", msg);
        }


        /**
         * 向kafka中塞入消息
         *
         * @param msg 消息
         */
        @GetMapping("/setKafkaMsg")
        public void setKafkaMsg(@RequestParam("msg") String msg) {
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

    public static void main(String[] args) {

    }

}
