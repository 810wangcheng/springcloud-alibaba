package com.cy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Administrator
 */
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
