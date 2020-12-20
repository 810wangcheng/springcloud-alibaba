package com.cy.service.fallback;

import com.cy.service.FeignService;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
public class ProviderFallback implements FeignService{

    @Override
    public String test(String message) {
        return "Request service has down,this is fallback result";
    }
}
