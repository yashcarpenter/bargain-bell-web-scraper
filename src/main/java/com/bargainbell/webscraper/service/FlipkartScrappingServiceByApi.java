package com.bargainbell.webscraper.service;


import com.google.gson.*;

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
public class FlipkartScrappingServiceByApi {

    @Async
    public CompletableFuture<String> fetchFlipkartProductPage(String url) {
        String apiUrl = "https://api.proxyscrape.com/v3/accounts/freebies/scraperapi/request";
        String apiKey = "f90fa158-dc6e-4024-8019-b1b49639290c";


        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create the POST request
            HttpPost post = new HttpPost(apiUrl);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("X-Api-Key", apiKey);

            // Payload for the request
            String payload = "{\"url\": \"" + url + "\", \"httpResponseBody\": true}";
            post.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

            // Execute the request and get the response
            CloseableHttpResponse response = httpClient.execute(post);
            String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            // Parse the JSON response
            JsonObject responseJson = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (responseJson.get("success").getAsBoolean()) {
                JsonObject data = responseJson.getAsJsonObject("data");

                // Extract the Base64 encoded HTML content from the "httpResponseBody" field
                String encodedHtmlContent = data.get("httpResponseBody").getAsString();

                // Decode the Base64 encoded HTML
                byte[] decodedBytes = Base64.getDecoder().decode(encodedHtmlContent);
                String decodedHtml = new String(decodedBytes, StandardCharsets.UTF_8);

                // Unescape any HTML entities
                String cleanHtml = StringEscapeUtils.unescapeHtml4(decodedHtml);

                return CompletableFuture.completedFuture(cleanHtml);
            } else {
                log.error("Failed to scrape the product page for flipkart");
            }
        } catch (Exception e) {
            log.error("Error fetching Flipkart product page for product code", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Double> getFlipkartProductPrice(String url) {
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> pageContentFuture = fetchFlipkartProductPage(url);
        String pageContent = pageContentFuture.join();  // Waiting for async result

        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        log.info("Time taken to fetch Flipkart Product Page By API: {} milliseconds", timeTaken);

        if (pageContent == null) {
            log.error("Page content for Flipkart page is null");
            return CompletableFuture.completedFuture(null);
        }

        try {
            // Parsing HTML with Jsoup
            Document doc = Jsoup.parse(pageContent);
            log.info("Fetched page content successfully.");

            // Search for all <script type="application/ld+json">
            Elements scripts = doc.select("script[type=application/ld+json]");
            log.info("Found {} <script> tags with type 'application/ld+json'", scripts.size());

            // Iterate through the script tags to find the relevant Product JSON
            for (Element script : scripts) {
                String jsonData = script.html();

                // Parse the JSON
                JsonElement jsonElement = JsonParser.parseString(jsonData);
                if (jsonElement.isJsonArray()) {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();

                    // Look for Product type object in the array
                    for (JsonElement element : jsonArray) {
                        if (element.isJsonObject()) {
                            JsonObject jsonObject = element.getAsJsonObject();
                            String type = jsonObject.get("@type").getAsString();
                            if (type.equals("Product")) {
                                // Extract the price from the 'offers' section
                                JsonObject offers = jsonObject.getAsJsonObject("offers");
                                int price = offers.get("price").getAsInt();
                                log.info("Found price: {} INR", price);
                                return CompletableFuture.completedFuture((double) price);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch the price for Flipkart Product for URL: {}", url, e);
        }
        return CompletableFuture.completedFuture(null);
    }
}
