package cn.running.demo.api;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * 华为DCS对接
 *
 * @Author hao cheng
 * @Date 2023/5/17 15:48
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/dcs")
public class HuaweiDcsApi {

    /**
     * StringRedisTemplate
     */
    private final StringRedisTemplate redisTemplate;


    /**
     * 向redis中塞入list数据类型数据
     *
     * @param key key
     */
    @GetMapping("/setRedisValue")
    public void setRedisValue(@RequestParam("key") String key) {
        // 字符串分割
        String[] str = new String[6];
        for (int i = 0; i < str.length; i++) {
            // 生成随机字符串
            str[i] = RandomUtil.randomString(6);
        }
        // 向redis中缓存list数据类型数据
        Long result = Optional.ofNullable(redisTemplate.boundListOps(key).leftPushAll(str)).orElse(0L);
        log.info("Redis缓存：{}！", result > 0L ? "成功" : "失败");
    }

    /**
     * 获取redis中的数据
     *
     * @param key key
     */
    @GetMapping("/getRedisValue")
    public void getRedisValue(@RequestParam("key") String key) {
        StringBuilder result = new StringBuilder();
        // 获取redis数据
        List<String> items = Optional.ofNullable(redisTemplate.boundListOps(key).range(0, -1))
                .orElse(Collections.emptyList());
        // 获取数据遍历append
        items.forEach(item -> result.append(" |-> ").append(item));
        log.info("获取缓存数据：{}", result);
    }
}
