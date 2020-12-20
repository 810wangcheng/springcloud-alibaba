package com.cy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
public class ProviderController {
    /** 读取yml中的配置端口 */
    @Value("${server.port}")
    private String port;

    @Value("${spring.application.name}")
    private String name;

    /** 自动注入客户端上下文内容 */
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @GetMapping(value = "/test/{message}")
    public String test(@PathVariable String message){
        return "Hello nacos discovery,received: "+message+" ,I am "+name+" from"+port;
    }

    @GetMapping(value = "/hi")
    public String sayHi(){
        return "Hello"+applicationContext.getEnvironment().getProperty("user.name");
    }
}
