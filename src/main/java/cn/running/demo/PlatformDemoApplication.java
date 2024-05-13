package cn.running.demo;

import cn.running.demo.api.HuaweiDcsApi;
import cn.running.demo.api.HuaweiMqApi;
import cn.running.demo.api.K8sApi;
import cn.running.demo.components.RocketMqConsumerComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {HuaweiMqApi.class, RocketMqConsumerComponent.class,
                HuaweiDcsApi.class, K8sApi.class}))
@SpringBootApplication(exclude = {})
public class PlatformDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformDemoApplication.class, args);
    }

}
