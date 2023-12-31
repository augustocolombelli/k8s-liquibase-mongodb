package com.product.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProductController {

    @GetMapping("/products")
    public String getProduct(){
        log.info("Products API was requested.");
        return "test";
    }
}
