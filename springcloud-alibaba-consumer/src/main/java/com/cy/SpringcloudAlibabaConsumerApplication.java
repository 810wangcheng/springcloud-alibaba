package com.cy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Administrator
 */
@SpringBootApplication
@EnableDiscoveryClient
public class SpringcloudAlibabaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringcloudAlibabaConsumerApplication.class, args);
    }

}
