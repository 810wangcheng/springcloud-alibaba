package com.cy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Administrator
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringcloudAlibabaProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringcloudAlibabaProviderApplication.class, args);
    }

}
