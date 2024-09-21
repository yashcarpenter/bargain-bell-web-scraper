package com.bargainbell.webscraper.service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class AmazonPriceScrapping {

    public String getAmazonPrice(String url) {
        try {
            // Set up headers for the request
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:91.0) Gecko/20100101 Firefox/91.0")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .header("DNT", "1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Referer", "https://www.google.com/")
                    .timeout(10000)  // Set timeout to avoid hanging
                    .get();

            // Print the first 500 characters of the page to debug
            String html = doc.html();
            System.out.println("HTML content (first 500 chars): " + html.substring(0, Math.min(html.length(), 500)));

            // Adjust the regex pattern for flexibility
            String searchPattern = "<span class=\"a-price-symbol\">₹</span><span class=\"a-price-whole\">([\\d,]+)</span>";
            Pattern pattern = Pattern.compile(searchPattern);
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                // Extract the price and remove commas
                String price = matcher.group(1).replace(",", "");
                return price;
            } else {
                System.out.println("Price tag not found in the response.");
                return null;
            }

        } catch (IOException e) {
            System.out.println("Failed to fetch the webpage: " + e.getMessage());
            return null;
        }
    }
}
