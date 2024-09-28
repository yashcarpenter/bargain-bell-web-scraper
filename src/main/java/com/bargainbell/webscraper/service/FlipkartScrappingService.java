package com.bargainbell.webscraper.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FlipkartScrappingService {

    @Async
    public CompletableFuture<Integer> fetchMyntraProductPrice(String productUrl) {
        log.info("Starting to fetch Myntra product page from URL: {}", productUrl);

        try {
            // Fetch the page content
            Document doc = Jsoup.connect(productUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .referrer("https://www.google.com")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .timeout(10 * 1000)  // Set timeout
                    .get();

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
                                return CompletableFuture.completedFuture(price);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error fetching the Myntra product page.", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public static String extractPidFromHtml(String url) {
        try {
            // Fetch the page content
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .referrer("https://www.google.com")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .timeout(10 * 1000)  // Set timeout
                    .get();

            System.out.println("Fetched page content successfully.");

            // Find the script containing "reviewButton"
            Elements scripts = doc.getElementsByTag("script");

            for (Element script : scripts) {
                String scriptContent = script.html();

                // Check if the script contains "reviewButton"
                if (scriptContent.contains("reviewButton")) {
                    // Use regex to find the pid value
                    Pattern pattern = Pattern.compile("\"pid\":\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);

                    if (matcher.find()) {
                        String pid = matcher.group(1);
                        System.out.println("Found PID: " + pid);
                        return pid;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error fetching the product page: " + e.getMessage());
        }
        return null;
    }
}