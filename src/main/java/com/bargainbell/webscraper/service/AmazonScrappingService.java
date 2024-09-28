package com.bargainbell.webscraper.service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class AmazonScrappingService {

  public Double getAmazonPriceAndAsin(String url) {
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
              .timeout(10000) // Set timeout to avoid hanging
              .get();

      // Fetch price
      Elements priceElements = doc.select("span.a-price-whole");
      Double price = null;
      if (!priceElements.isEmpty()) {
        String priceText = priceElements.get(0).text().replace(",", "");
        price = Double.parseDouble(priceText);
      } else {
        System.out.println("Price tag not found in the response.");
      }

      // Extract ASIN from the 'var opts' section
      String html = doc.html();
      Pattern pattern = Pattern.compile("var opts = \\{[^}]*?asin: \"([A-Z0-9]+)\"[^}]*?\\};", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(html);
      String asin = null;
      if (matcher.find()) {
        asin = matcher.group(1);
        System.out.println("ASIN found: " + asin);
      } else {
        System.out.println("ASIN not found in the response.");
      }

      return price;

    } catch (IOException e) {
      System.out.println("Failed to fetch the webpage: " + e.getMessage());
      return null;
    }
  }
}
