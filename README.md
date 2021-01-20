## springcloud-alibaba-demo
参考博客：https://www.jianshu.com/p/9a8d94c0c90c

项目地址：https://github.com/MoreBetterShare/springcloud-alibaba/tree/master
### 项目创建
1. 父级创建maven的pom项目
2. 模块创建springboot项目
3. 公共模块创建maven项目

### nacos注册中心
下载地址：https://github.com/alibaba/nacos/releases
#### window
##### 安装使用
- 启动：双击安装根目录bin/startup.cmd
- 测试url：http://127.0.0.1:8848/nacos/
- 配置：添加依赖，在application.yml或.properties如下配置
```
spring.cloud.nacos.discovery.server-addr:127.0.0.1:8848

 <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

```
- 相关服务在主启动类中添加：@EnableDiscoveryClient

经过上面的配置之后，在测试地址中便可以看到相关的服务

### 添加提供者模块
- 主启动类添加@EnableDiscoveryClient

#### 消费者配置文件
```
#配置端口号
server:
  port: 8083
#配置该服务名称
spring:
  application:
    name: nacos-provider
#配置nacos地址
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848

#actutor暴露端口
management:
  endpoints:
    web:
      exposure:
        include: "*"
#配置日志
logging:
  level:
    com.cy: debug
```

### 添加消费者模块
- 主启动类添加@EnableDiscoveryClient
- 实现远程服务调用
    - 配置远程调用restTemplet
```
@Configuration
public class ConsumerConfiguration {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
```
- 实现负载均衡：LoadBalancerClient
```
@RestController
public class ConsumerController {
    /**负载均衡客户端*/
    @Autowired
    private LoadBalancerClient balancerClient;

    /** restful类型的请求对象 */
    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.application.name}")
    private String name;

    @GetMapping(value = "/test/name")
    public String test(){
        ServiceInstance serviceInstance = balancerClient.choose("nacos-provider");
        String url = String.format("http://%s:%s/test/%s",serviceInstance.getHost(),serviceInstance.getPort(),name);
        return restTemplate.getForObject(url,String.class);
    }
}
```

#### feign声明式客户端：实现降级熔断
#### 使用
- 添加依赖
```
 <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```
- 主启动类添加@EnableDiscoveryClient和@EnableFeignClients
- 声明服务接口,直接调用提供者@FeignClient(value="nacos-provider")
- 熔断实现写xxxFallback类继承上面的接口，并修改@FeignClient(value="nacos-provider",Fallback=xxxFallback.class)
- 添加配置防止后面解析string报错
```
@SpringBootConfiguration
public class FeignConfig {
    @Bean
    public Decoder feignDecoder() {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverter()));
    }

    public ObjectFactory<HttpMessageConverters> feignHttpMessageConverter() {
        final HttpMessageConverters httpMessageConverters = new HttpMessageConverters(new GateWayMappingJackson2HttpMessageConverter());
        return new ObjectFactory<HttpMessageConverters>() {
            @Override
            public HttpMessageConverters getObject() throws BeansException {
                return httpMessageConverters;
            }
        };
    }

    public class GateWayMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {
        GateWayMappingJackson2HttpMessageConverter(){
            List<MediaType> mediaTypes = new ArrayList<>();
            mediaTypes.add(MediaType.valueOf(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8"));
            setSupportedMediaTypes(mediaTypes);
        }
    }

}
```
#### 消费者配置文件：application.yml
```
#配置该服务名称
spring:
  application:
    name: nacos-feign

#配置nacos注册中心
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
#配置仪表盘
    sentinel:
      transport:
        port: 8719
        dashboard: localhost:8080

#配置启动端口
server:
  port: 8082

#检测端口
management:
  endpoints:
    web:
      exposure:
        include: "*"

#配置sentinel实现熔断
feign:
  sentinel:
    enabled: true
```

#### sentinel控制台：仪表盘
- 下载地址：https://github.com/alibaba/Sentinel/releases
- 启动
```
java -jar D:\810chengData\projectManage\sentinel-dashboard.jar

java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```
- 配置
```
spring.cloud.sentinel.transport.port: 8719
spring.cloud.sentinel.transport.dashbord: localhost:8080
```
- 保证客户端有一定的访问量，然后进入仪表盘查看

### gateway网关
- 添加依赖
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```
- 主启动类添加：@EnableDiscoveryClient和@EnableFeignClients

启动项目，先访问网关，然后由网关调用配置的服务

- 添加过滤配置
```
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getQueryParams().getFirst("token");
        if(token == null ||token.isEmpty()){
            ServerHttpResponse response = exchange.getResponse();
            HashMap<String, Object> responseMap = Maps.newHashMap();
            responseMap.put("code",401);
            responseMap.put("message","非法请求");
            responseMap.put("cause","token is empty");

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                byte[] bytes = objectMapper.writeValueAsBytes(responseMap);
                DataBuffer wrap = response.bufferFactory().wrap(bytes);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type","application/json;charset=UTF-8");
                return response.writeWith(Mono.just(wrap));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
```
#### 网关配置文件
```
spring:
  application:
    name: springcloud-gateway
#nacos注册中心
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
#配置仪表盘
    sentinel:
      transport:
        port: 8720
        dashboard: localhost:8080
#配置网关
    gateway:
      discovery:
        locator:
          enabled: true
      #配置路由规则
      routes:
        #采用自定义路由ID
        - id: NACOS-CONSUMER
          #采用LoadBalanceClient方式请求，
          #以lb://开头，后面是注册在nacos中的服务名
          uri: lb://nacos-consumer
          predicates:
            - Method=GET,POST
        - id: NACOS-FEIGN
          uri: lb://nacos-feign
          predicates:
            - Method=GET,POST
server:
  port: 8086

feign:
  sentinel:
    enabled: true

management:
  endpoints:
    web:
      exposure:
        include: "*"

#配置日志
logging:
  level:
    com.cy: debug
```
访问测试：

http://localhost:9000/nacos-consumer-feign/test/hi

http://localhost:9000/nacos-consumer/test/app/name

### nacos配置中心
- 访问：http://127.0.0.1:8848/nacos/
- 配置管理，创建配置文件默认使用properties，可以自己选择
- 添加依赖
```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```
- 创建bootstrap.properties,并移动yml文件到nacos中，并删除原项目中的yml文件
- 修改bootstrap.properties如下
```
#对应nacos config中的DataID
spring.application.name=nacos-provider-config
#配置文件类型
spring.cloud.nacos.config.file-extension= yaml
#配置nacos config地址
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```
- 配置完成后重启nacos,进行访问测试

开发过程中：服务提供者会提供接口供服务消费者进行调用


### 报错汇总
1. 
```
Description:

Parameter 0 of method modifyResponseBodyGatewayFilterFactory in org.springframework.cloud.gateway.config.GatewayAutoConfiguration required a bean of type 'org.springframework.http.codec.ServerCodecConfigurer' that could not be found.
```
 spring-boot-start-web与gateway产生了冲突，将spring-boot-start-web注掉可解决

2. 参考博客：https://blog.csdn.net/lizz861109/article/details/105707590
