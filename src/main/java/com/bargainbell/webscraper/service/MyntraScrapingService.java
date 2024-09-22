package com.bargainbell.webscraper.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class MyntraScrapingService {

    public Double getMyntraProductPrice(String productCode) {
        String url = "https://www.myntra.com/" + productCode;
        System.out.println("Fetching product price for URL: " + url);

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "en-US,en;q=0.5");
            headers.put("Connection", "keep-alive");
            headers.put("Referer", "https://www.myntra.com/");

            System.out.println("Headers set: " + headers);

            Document doc = Jsoup.connect(url)
                    .headers(headers)
                    .timeout(30000)
                    .method(Connection.Method.GET)
                    .get();

            System.out.println("Successfully fetched the document for URL: " + url);

            System.out.println("Document HTML: " + doc.html());

            Elements scripts = doc.select("script[type=application/ld+json]");
            System.out.println("Scripts found: " + scripts.size());

            for (Element script : scripts) {
                String jsonStr = script.html().trim();
                System.out.println("Found script tag with JSON: " + jsonStr);

                if (jsonStr.isEmpty()) {
                    continue;
                }

                try {
                    JsonObject jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();  // Parse it as JSON
                    System.out.println("Parsed JSON Object: " + jsonObj);

                    if (jsonObj.has("@type") && jsonObj.get("@type").getAsString().equals("Product")) {
                        String productName = jsonObj.has("name") ? jsonObj.get("name").getAsString() : "N/A";
                        JsonObject offers = jsonObj.getAsJsonObject("offers");
                        String price = offers.has("price") ? offers.get("price").getAsString() : "N/A";
                        String availability = offers.has("availability") ? offers.get("availability").getAsString() : "N/A";

                        System.out.println("Product Name: " + productName);
                        System.out.println("Price: " + price);
                        System.out.println("Availability: " + availability);

                        return Double.parseDouble(price);
                    }
                } catch (JsonSyntaxException e) {
                    System.out.println("Failed to parse JSON for Product Code: " + productCode);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch the price for Product Code: " + productCode);
            e.printStackTrace();
        }
        System.out.println("Returning null");
        return null;
    }
}
