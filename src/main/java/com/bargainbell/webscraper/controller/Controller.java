package com.bargainbell.webscraper.controller;


import com.bargainbell.webscraper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    private AmazonScrappingService amazonScrappingService;
    @Autowired
    private ProxyChecker proxyChecker;
    @Autowired
    private MyntraScrapingServiceByAPI myntraScrapingServiceByAPI;
    @Autowired
    private FlipkartScrappingServiceByApi flipkartScrappingServiceByApi;
    @Autowired FlipkartScrappingService flipkartScrappingService;

    @RequestMapping(value = "/myntra/{productCode}", method = RequestMethod.GET)
    public Double getPriceFromMyntra(@PathVariable String productCode){
        CompletableFuture<Double> futurePrice = myntraScrapingServiceByAPI.getMyntraProductPrice(productCode);
        Double minPrice = futurePrice.join();
        return minPrice;
    }

    @RequestMapping(value = "/flipkart", method = RequestMethod.GET)
    public Double getPriceFromFlipkart(@RequestParam String url){
        CompletableFuture<Double> futurePrice = flipkartScrappingServiceByApi.getFlipkartProductPrice(url);
        return futurePrice.join();
    }

    @RequestMapping(value = "/flipkart/getpid", method = RequestMethod.GET)
    public String getPidFromFlipkart(@RequestParam String url){
        return flipkartScrappingService.extractPidFromHtml(url);
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

    @RequestMapping(value = "/proxy/amazon-scrape", method = RequestMethod.GET)
    public Double getPriceFromAmazonScapeProxy(@RequestParam String url){
        return amazonScrapingServiceByProxy.getAmazonProductPrice(url);
    }

    @RequestMapping(value = "/amazon-scrape", method = RequestMethod.GET)
    public Double getPriceFromAmazonScape(@RequestParam String url){
        return amazonScrappingService.getAmazonPriceAndAsin(url);
    }

    @GetMapping("/check-proxies")
    public List<String> checkProxies() throws IOException {
        System.out.println("Checking");
        return proxyChecker.checkProxies(); // Trigger the proxy checking and return the list of working proxies
    }

}
