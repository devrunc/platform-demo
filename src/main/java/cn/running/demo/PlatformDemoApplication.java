package cn.running.demo;

import cn.running.demo.api.HuaweiDcsApi;
import cn.running.demo.api.HuaweiRomaApi;
import cn.running.demo.api.K8sApi;
import cn.running.demo.components.RocketMqConsumerComponent;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {HuaweiRomaApi.MQS.class, RocketMqConsumerComponent.class,
                HuaweiDcsApi.class, K8sApi.class}))
@SpringBootApplication
public class PlatformDemoApplication {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurer(@Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }

    public static void main(String[] args) {
        SpringApplication.run(PlatformDemoApplication.class, args);
    }

}
