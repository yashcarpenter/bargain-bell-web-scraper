package com.bargainbell.webscraper.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64; // Add this import for Base64
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyntraScrapingServiceByAPI {

    @Async
    public CompletableFuture<String> fetchMyntraProductPage(String productCode) {
        String targetUrl = "https://www.myntra.com/" + productCode;
        String apiUrl = "https://api.proxyscrape.com/v3/accounts/freebies/scraperapi/request";
        String apiKey = "f90fa158-dc6e-4024-8019-b1b49639290c";

        log.info("Starting to fetch Myntra product page for product code: {}", productCode);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the POST request
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("X-Api-Key", apiKey);

            // Payload for the request
            String payload = "{\"url\": \"" + targetUrl + "\", \"httpResponseBody\": true}";
            post.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

            log.info("Sending request to ProxyScrape API for product code: {}", productCode);

            // Execute the request and get the response
            CloseableHttpResponse response = httpClient.execute(post);
            String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.info("Received response from ProxyScrape API for product code: {}", productCode);

            // Parse the JSON response
            JsonObject responseJson = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (responseJson.get("success").getAsBoolean()) {
                JsonObject data = responseJson.getAsJsonObject("data");

                // Extract the Base64 encoded HTML content from the "httpResponseBody" field
                String encodedHtmlContent = data.get("httpResponseBody").getAsString();

                log.info("Successfully extracted Base64-encoded HTML content for product code: {}", productCode);

                // Decode the Base64 encoded HTML
                byte[] decodedBytes = Base64.getDecoder().decode(encodedHtmlContent);
                String decodedHtml = new String(decodedBytes, StandardCharsets.UTF_8);

                // Unescape any HTML entities
                String cleanHtml = StringEscapeUtils.unescapeHtml4(decodedHtml);

                log.info("Successfully decoded and cleaned HTML content for product code: {}", productCode);

                return CompletableFuture.completedFuture(cleanHtml);
            } else {
                log.error("Failed to scrape the product page for product code: {}", productCode);
            }
        } catch (Exception e) {
            log.error("Error fetching Myntra product page for product code: {}", productCode, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Double> getMyntraProductPrice(String productCode) {
        log.info("Fetching Myntra Product Price by Proxy for product code: {}", productCode);
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> pageContentFuture = fetchMyntraProductPage(productCode);
        String pageContent = pageContentFuture.join();  // Waiting for async result

        System.out.println(pageContent);

        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        log.info("Time taken to fetch Myntra Product Page through proxy for product code {}: {} milliseconds", productCode, timeTaken);

        if (pageContent == null) {
            log.error("Failed to fetch page content for product code: {}", productCode);
            return CompletableFuture.completedFuture(null);
        }

        try {
            // Parsing HTML with Jsoup
            Document doc = Jsoup.parse(pageContent);
            log.info("Parsed HTML document for product code: {}", productCode);

            // Extract the relevant script containing the product data
            Elements scripts = doc.select("script[type=application/ld+json]");
            log.info("Found {} <script> tags in HTML for product code: {}", scripts.size(), productCode);

            for (Element script : scripts) {
                String jsonStr = script.html().trim();
                if (!jsonStr.isEmpty()) {
                    log.info("Processing JSON script content for product code: {}", productCode);
                    try {
                        JsonObject jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();

                        if (jsonObj.has("@type") && jsonObj.get("@type").getAsString().equals("Product")) {
                            String productName = jsonObj.has("name") ? jsonObj.get("name").getAsString() : "N/A";
                            JsonObject offers = jsonObj.getAsJsonObject("offers");
                            String price = offers.has("price") ? offers.get("price").getAsString() : "N/A";
                            String availability = offers.has("availability") ? offers.get("availability").getAsString() : "N/A";

                            log.info("Product found: {}, Price: {}, Availability: {}", productName, price, availability);

                            return CompletableFuture.completedFuture(Double.parseDouble(price));
                        }
                    } catch (JsonSyntaxException e) {
                        log.error("Failed to parse JSON for Product Code: {}", productCode, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch the price for Product Code: {}", productCode, e);
        }
        return CompletableFuture.completedFuture(null);
    }
}

