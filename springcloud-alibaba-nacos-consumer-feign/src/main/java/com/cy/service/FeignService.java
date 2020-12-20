package com.cy.service;

import com.cy.service.fallback.ProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Administrator
 */
@FeignClient(value = "nacos-provider")
public interface FeignService {

    /** 调用nocos-provider服务 */
    @GetMapping(value = "test/{message}")
    String test(@PathVariable("message") String message);


}
