package cn.running.demo.api;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.running.demo.common.CommonConstant;
import cn.running.demo.properties.HuaweiProperties;
import com.huaweicloud.sdk.aom.v2.AomClient;
import com.huaweicloud.sdk.aom.v2.model.ListAgentsRequest;
import com.huaweicloud.sdk.aom.v2.model.ListAgentsResponse;
import com.huaweicloud.sdk.cce.v3.CceClient;
import com.huaweicloud.sdk.cce.v3.model.*;
import com.huaweicloud.sdk.core.HcClient;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.http.HttpConfig;
import com.huaweicloud.sdk.ges.v1.GesClient;
import com.huaweicloud.sdk.ges.v1.model.ListGraphsRequest;
import com.huaweicloud.sdk.ges.v1.model.ListGraphsResponse;
import com.huaweicloud.sdk.iam.v3.IAMCredentials;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.*;
import com.huaweicloud.sdk.roma.v2.RomaClient;
import com.huaweicloud.sdk.roma.v2.model.CreateApiGroupV2Request;
import com.huaweicloud.sdk.roma.v2.model.CreateApiGroupV2Response;
import com.huaweicloud.sdk.roma.v2.model.CreateProductTopicRequest;
import com.huaweicloud.sdk.roma.v2.model.CreateProductTopicResponse;
import com.huaweicloud.sdk.swr.v2.SwrClient;
import com.huaweicloud.sdk.swr.v2.model.CreateNamespaceRequest;
import com.huaweicloud.sdk.swr.v2.model.CreateNamespaceRequestBody;
import com.huaweicloud.sdk.swr.v2.model.CreateNamespaceResponse;
import com.obs.services.ObsClient;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectResult;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * 华为云SDK对接
 *
 * @Author hao cheng
 * @Date 2023/5/16 10:14
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/sdk")
public class HuaweiSdkApi {

    /**
     * HuaweiProperties
     */
    private final HuaweiProperties properties;

    /**
     * 创建请求客户端
     */
    private static final HttpConfig CONFIG = new HttpConfig()
            // 超时时间
            .withTimeout(30)
            // 跳过SSL证书验证
            .withIgnoreSSLVerification(true);

    /**
     * 认证
     */
    private static ICredential CREDENTIALS;


    /**
     * 创建SWR命名空间
     */
    @GetMapping("/createSwrNamespace")
    public String createSwrNamespace(@RequestParam("name") String name) {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.SWR_API_SERVICE_NAME);
        // build SWR Client
        SwrClient swrClient = SwrClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求创建命令空间
        CreateNamespaceResponse namespaceResponse = swrClient.createNamespace(new CreateNamespaceRequest()
                // 构造请求
                .withBody(new CreateNamespaceRequestBody()
                        // 命名空间名称
                        .withNamespace(name)));
        log.info("请求创建SWR命令空间：{}", namespaceResponse.getHttpStatusCode());
        return namespaceResponse.toString();
    }

    /**
     * 创建ROMA-API分组
     *
     * @param instanceId 实例id
     * @param appId      集成应用id
     * @param name       分组名称
     */
    @GetMapping("createApiGroup")
    public String createApiGroup(@RequestParam("instanceId") String instanceId,
                                 @RequestParam("appId") String appId,
                                 @RequestParam("name") String name) {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.ROMA_SERVICE_NAME);
        // build ROMA Client
        RomaClient romaClient = RomaClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求创建API分组
        CreateApiGroupV2Response apiGroupResponse = romaClient.createApiGroupV2(new CreateApiGroupV2Request()
                // 实例id
                .withInstanceId(instanceId)
                // 构造请求
                .withBody(request -> request
                        // 分组名称
                        .withName(name)
                        // 集成应用id
                        .withRomaAppId(appId)
                        // 分组版本：V1：全局分组 - V2：应用级分组
                        .withVersion("V2")
                        .withRemark("接口调用创建的API分组。")));
        log.info("请求创建ROMA-API分组：{}", apiGroupResponse.getHttpStatusCode());
        return apiGroupResponse.toString();
    }

    /**
     * 创建ROMA-MQS-Topic
     *
     * @param instanceId 实例id
     * @param topicName  topicName
     */
    @GetMapping("/createKafkaTopic")
    public String createKafkaTopic(@RequestParam("instanceId") String instanceId,
                                   @RequestParam("topicName") String topicName) {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.ROMA_SERVICE_NAME);
        // build ROMA Client
        RomaClient romaClient = RomaClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求创建ROMA-MQS-Topic
        CreateProductTopicResponse topicResponse = romaClient.createProductTopic(new CreateProductTopicRequest()
                .withInstanceId(instanceId)
                .withBody(request -> {
                    request.withName(topicName);
                }));
        log.info("请求创建ROMA-MQS-Topic：{}", topicResponse.getHttpStatusCode());
        return topicResponse.toString();
    }

    /**
     * 创建CCE集群
     */
    @GetMapping("/createCceCluster")
    public String createCceCluster() {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.CCE_SERVICE_NAME);
        // build CCE Client
        CceClient cceClient = CceClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求创建集群
        CreateClusterResponse clusterResponse = cceClient.createCluster(new CreateClusterRequest()
                // 构造请求
                .withBody(cluster -> cluster
                        // API类型
                        .withKind("cluster")
                        // API版本
                        .withApiVersion("v3")
                        // 集群的基础信息
                        .withSpec(spec -> spec
                                // 集群版本：v1.21、v1.19
                                .withVersion("v1.21")
                                // 集群类型：CCE、Turbo
                                .withCategory(ClusterSpec.CategoryEnum.CCE)
                                // Master节点架构：X86、ARM64
                                .withType(ClusterSpec.TypeEnum.VIRTUALMACHINE)
                                /*
                                集群规格：
                                    单Master50节点：cce.s1.small
                                    单Master200节点：cce.s1.medium
                                    三Master50节点：cce.s2.small
                                    三Master200节点：cce.s2.medium
                                    三Master1000节点：cce.s2.large
                                    三Master2000节点：cce.s2.xlarge
                                 */
                                .withFlavor("cce.s2.small")
                                // 集群是否启用ipv6
                                .withIpv6enable(Boolean.FALSE)
                                .withHostNetwork(hostNetwork -> hostNetwork
                                        // 虚拟私有云id
                                        .withVpc("15a30917-66a3-463d-9a75-b5703b7db956")
                                        // 控制节点子网id
                                        .withSubnet("e121813b-5766-41a6-80ac-3fdbd42a3cce"))
                                .withContainerNetwork(containerNetwork -> containerNetwork
                                        // 容器网络类型：VPC、overlay_l2、eni
                                        .withMode(ContainerNetwork.ModeEnum.VPC_ROUTER)
                                        // 容器网段：10.247.0.0/16、172.16.0.0/16、192.168.0.0/16
                                        .withCidr("111.16.0.0/16"))
                                // 服务网段参数：10.247.0.0/16、172.16.0.0/16、192.168.0.0/16
                                .withKubernetesSvcIpRange("111.26.0.0/16")
                                // 服务转发模式：iptables、ipvs
                                .withKubeProxyMode(ClusterSpec.KubeProxyModeEnum.IPTABLES)
                                // 集群控制节点AZ
                                .withMasters(masterSpecs -> {
                                    // 三台Master定义不同AZ
                                    masterSpecs.add(new MasterSpec().withAvailabilityZone("x86.yd_hlw"));
                                    masterSpecs.add(new MasterSpec().withAvailabilityZone("x86.yd_hlw"));
                                    masterSpecs.add(new MasterSpec().withAvailabilityZone("xcx86.yd_hlw"));
                                })
                                // 集群扩展字段
                                .withExtendParam(extendParam -> extendParam
                                        // 容器网络固定IP池掩码位数：24 ~ 28
                                        .withAlphaCceFixPoolMask("24")
                                        // 集群CPU管理策略：none、static
                                        .withKubernetesIoCpuManagerPolicy("none")))
                        // 集群的元信息
                        .withMetadata(metadata -> metadata
                                // 集群名称
                                .withName("api-test-cce")
                                .withAnnotations(map -> {
                                    // 安装icAgent
                                    map.put("cluster.install.addons.external/install", "\"[{\"addonTemplateName\":\"icagent\"}]");
                                }))));
        log.info("请求创建CCE集群：{}", clusterResponse.getHttpStatusCode());
        return clusterResponse.toString();
    }

    /**
     * 查询图列表
     */
    @GetMapping("listGesGraphs")
    public String listGesGraphs() {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.GES_SERVICE_NAME);
        // build GES Client
        GesClient gesClient = GesClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求查询图列表
        ListGraphsResponse listGraphsResponse = gesClient.listGraphs(new ListGraphsRequest());
        log.info("请求查询GES图列表：{}", listGraphsResponse.getHttpStatusCode());
        listGraphsResponse.getGraphs().forEach(info -> log.info(info.toString()));
        return listGraphsResponse.toString();
    }

    /**
     * 查询主机的ICAgent信息
     */
    @GetMapping("listAgents")
    public String listAgents() {
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.AOM_INTERFACE);
        // build AOM Client
        AomClient aomClient = AomClient.newBuilder()
                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
                .withCredential(CREDENTIALS)
                .build();
        // 请求查询主机的ICAgent信息
        ListAgentsResponse agentsResponse = aomClient.listAgents(new ListAgentsRequest()
                .withClusterId("8830ef95-d08e-11ee-ac15-0255ac1000ab")
                .withNamespace("default"));
        log.info("请求查询主机的ICAgent信息：{}", agentsResponse.getHttpStatusCode());
        return agentsResponse.toString();
    }

    @GetMapping("/iam")
    public void iam() {
        KeystoneCreateUserTokenByPasswordRequest passwordRequest = new KeystoneCreateUserTokenByPasswordRequest()
                .withBody(new KeystoneCreateUserTokenByPasswordRequestBody()
                        .withAuth(new PwdAuth()
                                .withScope(new AuthScope()
                                        .withProject(new AuthScopeProject()
                                                .withId(properties.getProjectId())))
                                .withIdentity(new PwdIdentity()
                                        .addMethodsItem(PwdIdentity.MethodsEnum.PASSWORD)
                                        .withPassword(new PwdPassword()
                                                .withUser(new PwdPasswordUser()
                                                        .withName("sjy-paas")
                                                        .withPassword("QAZ2wsx@123!")
                                                        .withDomain(new PwdPasswordUserDomain()
                                                                .withName("济南市公安局")))))));
        // 构建完整URL
        String endpoint = buildEndpoint(CommonConstant.IAM_INTERFACE);
        IamClient iamClient = IamClient.newBuilder()
//                .withEndpoints(ListUtil.toList(endpoint))
                .withHttpConfig(CONFIG)
//                .withCredential(new GlobalCredentials()
//                        .withAk(properties.getAccessKey())
//                        .withSk(properties.getSecretKey()))
                .withCredential(new IAMCredentials())
                .build();
        KeystoneCreateUserTokenByPasswordResponse tokenResponse = iamClient.keystoneCreateUserTokenByPassword(passwordRequest);
        log.info("请求调用IAM：{}", tokenResponse.getHttpStatusCode());
        log.info(tokenResponse.toString());
    }

    /**
     * 获取obs对象文件
     *
     * @param bucketName 桶名称
     */
    @GetMapping("/getObsObject")
    public String getObsObject(@RequestParam("name") String bucketName) throws IOException {
        // 创建ObsClient实例
        final ObsClient obsClient = new ObsClient(properties.getAccessKey(), properties.getSecretKey(),
                buildEndpoint(CommonConstant.OBS_V3_INTERFACE));
        // 获取所有对象文件
        ObjectListing obsList = obsClient.listObjects(bucketName);
        obsList.getObjects().forEach(info -> {
            System.out.println(info.toString());
        });
        // 指定获取obs对象文件
        ObsObject obsObj = obsClient.getObject(bucketName, "123.txt");
        // 获取输入流
        InputStream objContent = obsObj.getObjectContent();
        if (ObjectUtil.isNotNull(objContent)) {
            BufferedReader reader = IoUtil.getUtf8Reader(objContent);
            StringBuilder builder = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (StrUtil.isBlank(line)) {
                    reader.close();
                    break;
                }
                builder.append(line);
            }
            // 保存obs对象文件
            FileUtil.writeUtf8String(builder.toString(), "C:\\123.txt");
        }
        obsClient.close();
        return obsList.toString();
    }

    /**
     * 上传obs对象文件
     *
     * @param bucketName 桶名称
     */
    @GetMapping("/putObsObject")
    public String putObsObject(@RequestParam("name") String bucketName) throws IOException {
        // 创建ObsClient实例
        final ObsClient obsClient = new ObsClient(properties.getAccessKey(), properties.getSecretKey(),
                buildEndpoint(CommonConstant.OBS_V3_INTERFACE));
        // 上传对象文件
        PutObjectResult result = obsClient.putObject(bucketName, "test.txt", FileUtil.file("C:\\123.txt"));
        return result.toString();
    }

    /**
     * 构建完整的地址
     *
     * @param prefix 前缀
     * @return 完整的地址
     */
    private String buildEndpoint(String prefix) {
        return StrUtil.format("{}.{}.{}", prefix, properties.getRegionId(), properties.getDomainName());
    }

    /**
     * 启动时构造认证
     */
    @PostConstruct
    private void buildBasicCredentials() {
        // 初始化
        CREDENTIALS = new BasicCredentials()
                .withAk(properties.getAccessKey())
                .withSk(properties.getSecretKey())
                .withProjectId(properties.getProjectId());
    }

}
