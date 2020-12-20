package com.cy.controller;

import com.cy.service.FeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
public class FeignController {

    @Autowired
    private FeignService feignService;

    @GetMapping(value = "test/hi")
    public String test(){
        return feignService.test("Hi feign");
    }
}
