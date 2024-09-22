package com.bargainbell.webscraper.controller;


import com.bargainbell.webscraper.service.AmazonApiService;
import com.bargainbell.webscraper.service.AmazonScrapingServiceByProxy;
import com.bargainbell.webscraper.service.FlipkartApiService;
import com.bargainbell.webscraper.service.MyntraScrapingServiceByProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webscrape")
public class Controller {

    @Autowired
    private MyntraScrapingServiceByProxy myntraScrapingServiceByProxy;
    @Autowired
    private FlipkartApiService flipkartApiService;
    @Autowired
    private AmazonApiService amazonApiService;
    @Autowired
    private AmazonScrapingServiceByProxy amazonScrapingServiceByProxy;

    @RequestMapping(value = "/myntra/{productCode}", method = RequestMethod.GET)
    public Double getPriceFromMyntra(@PathVariable String productCode){
        return myntraScrapingServiceByProxy.getMyntraProductPrice(productCode);
    }

    @RequestMapping(value = "/flipkart", method = RequestMethod.GET)
    public Double getPriceFromFlipkart(@RequestParam String url){
        return flipkartApiService.getFlipkartProductPrice(url);
    }

    @RequestMapping(value = "/amazon/{asin}", method = RequestMethod.GET)
    public Double getPriceFromAmazon(@PathVariable String asin){
        long startTime = System.currentTimeMillis();
        Double price = amazonApiService.getProductPrice(asin);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Time taken to fetch price: " + timeTaken + " milliseconds");
        return price;
    }

    @RequestMapping(value = "/amazon-scrape", method = RequestMethod.GET)
    public Double getPriceFromAmazonScape(@RequestParam String url){
        return amazonScrapingServiceByProxy.getAmazonProductPrice(url);
    }

}
