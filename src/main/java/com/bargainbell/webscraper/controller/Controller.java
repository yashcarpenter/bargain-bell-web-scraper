package com.bargainbell.webscraper.controller;


import com.bargainbell.webscraper.service.AmazonApiService;
import com.bargainbell.webscraper.service.AmazonPriceScrapping;
import com.bargainbell.webscraper.service.FlipkartApiService;
import com.bargainbell.webscraper.service.MyntraScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webscrape")
public class Controller {

    @Autowired
    private MyntraScrapingService myntraScrapingService;
    @Autowired
    private FlipkartApiService flipkartApiService;
    @Autowired
    private AmazonApiService amazonApiService;
    @Autowired
    private AmazonPriceScrapping amazonPriceScrapping;

    @RequestMapping(value = "/myntra/{productCode}", method = RequestMethod.GET)
    public Double getPriceFromMyntra(@PathVariable String productCode){
        return myntraScrapingService.getMyntraProductPrice(productCode);
    }

    @RequestMapping(value = "/flipkart/{pid}", method = RequestMethod.GET)
    public Double getPriceFromFlipkart(@PathVariable String pid){
        return flipkartApiService.getFlipkartProductPrice(pid);
    }
    @RequestMapping(value = "/amazon/{asin}", method = RequestMethod.GET)
    public Double getPriceFromAmazon(@PathVariable String asin){
        return amazonApiService.getProductPrice(asin);
    }
    @RequestMapping(value = "/amazon-scrape", method = RequestMethod.GET)
    public String getPriceFromAmazonScape(@RequestParam String url){
        return amazonPriceScrapping.getAmazonPrice(url);
    }

}
