package com.bargainbell.webscraper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AmazonApiService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String API_URL =
            "https://amazon-scrapper-api3.p.rapidapi.com/products/{asin}?api_key=17fd230b65a63c27854fdb057d95524c";
    private static final String RAPIDAPI_HOST = "amazon-scrapper-api3.p.rapidapi.com";
    private static final String RAPIDAPI_KEY = "621d292537mshc52561248cd1788p18bd91jsn0c3f456b3009";

    public String fetchProductPrice(String asin) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", RAPIDAPI_HOST);
        headers.set("x-rapidapi-key", RAPIDAPI_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class, asin);

        return response.getBody();
    }

    public Double getProductPrice(String asin) {
        String response = fetchProductPrice(asin);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode productPriceNode = root.path("pricing");

            if (productPriceNode.isMissingNode() || productPriceNode.asText().isEmpty()) {
                return null;
            }

            String priceString = productPriceNode.asText();

            String cleanedPriceString = priceString.replaceAll("[^\\d.]", "");

            return Double.parseDouble(cleanedPriceString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
