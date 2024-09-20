package com.bargainbell.webscraper.controller;


import com.bargainbell.webscraper.service.FlipkartApiService;
import com.bargainbell.webscraper.service.MyntraScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webscrape")
public class Controller {

    @Autowired
    private MyntraScrapingService myntraScrapingService;

    @Autowired
    private FlipkartApiService flipkartApiService;

    @RequestMapping(value = "/myntra/{productCode}", method = RequestMethod.GET)
    public Double getPriceFromMyntra(@PathVariable String productCode){
        return myntraScrapingService.getMyntraProductPrice(productCode);
    }

    @RequestMapping(value = "/flipkart/{pid}", method = RequestMethod.GET)
    public Double getPriceFromFlipkart(@PathVariable String pid){
        return flipkartApiService.getFlipkartProductPrice(pid);
    }
}
