package cn.running.demo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * HuaweiProperties
 *
 * @Author hao cheng
 * @Date 2023/5/16 16:54
 */
@Data
@Component
@ConfigurationProperties(prefix = "huawei")
public class HuaweiProperties {

    /**
     * Region_id
     */
    private String regionId;

    /**
     * 内部Global域名
     */
    private String domainName;

    /**
     * Secret Access Key
     */
    private String accessKey;

    /**
     * Secret Access Key
     */
    private String secretKey;

    /**
     * 资源集id
     */
    private String projectId;

}
