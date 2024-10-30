package cn.running.demo.api;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.running.demo.common.CommonConstant;
import cn.running.demo.properties.HuaweiProperties;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * K8S Api对接
 *
 * @Author hao cheng
 * @Date 2023/5/17 9:39
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/k8s")
public class K8sApi {

    /**
     * HuaweiProperties
     */
    private final HuaweiProperties properties;

    /**
     * build K8S client
     */
    private static final KubernetesClient CLIENT = new KubernetesClientBuilder()
//            .withConfig(Config.fromKubeconfig(FileUtil.readUtf8String("/tmp/k8s/kubeconfig.json")))
            .withConfig(new ConfigBuilder().withMasterUrl("http://127.0.0.1:8001").build())
            .build();

    /**
     * 查询集群相关信息
     */
    @GetMapping("/getClusterInfo")
    public void getClusterInfo() {
        log.info("当前集群版本：{}", CLIENT.getKubernetesVersion().getGitVersion());
        // 获取所有pod
        CLIENT.pods().list().getItems().forEach(item -> {
            log.info("{} \t pod/{}", item.getMetadata().getNamespace(), item.getMetadata().getName());
        });
    }

    /**
     * 创建Deploy
     */
    @GetMapping("/createDeploy")
    public void createDeploy() {
        // 创建无状态工作负载
        Deployment deployment = CLIENT.apps().deployments().inNamespace("me")
                .resource(new DeploymentBuilder()
                        .withNewMetadata()
                        // Deploy名称
                        .withName("test-deploy")
                        // labels
                        .withLabels(MapUtil.of("app", "test-deploy"))
                        .endMetadata()
                        .withNewSpec()
                        // 副本数
                        .withReplicas(1)
                        .withNewTemplate()
                        .withNewSpec()
                        // 容器信息
                        .withContainers(new ContainerBuilder()
                                .withName("container1")
                                .withImage(StrUtil.format("{}.{}.{}/images-group/nginx:1.23.2-alpine", CommonConstant.SWR_SERVICE_NAME, properties.getRegionId(), properties.getDomainName()))
                                .withImagePullPolicy("IfNotPresent")
                                .addNewPort()
                                .withContainerPort(80)
                                .endPort()
                                .build())
                        .endSpec()
                        .endTemplate()
                        .endSpec()
                        .build())
                .create();
        log.info(deployment.getStatus().toString());
    }
}
