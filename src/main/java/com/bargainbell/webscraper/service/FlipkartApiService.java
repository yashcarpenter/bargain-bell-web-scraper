package com.bargainbell.webscraper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FlipkartApiService {
  @Autowired private RestTemplate restTemplate;
  private static final String API_URL =
      "https://real-time-flipkart-api.p.rapidapi.com/product-details?pid={pid}";
  private static final String RAPIDAPI_HOST = "real-time-flipkart-api.p.rapidapi.com";
  private static final String RAPIDAPI_KEY = "621d292537mshc52561248cd1788p18bd91jsn0c3f456b3009";

  public Double getFlipkartProductPrice(String input) {
    String pid = getFlipkartProductId(input);
    String response = fetchPrice(pid);
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response);
      JsonNode productPriceNode = root.path("price");
      String priceString = productPriceNode.asText();
      Pattern pattern = Pattern.compile("[^\\d.]+");
      Matcher matcher = pattern.matcher(priceString);
      String numericPriceString = matcher.replaceAll("");
      return Double.parseDouble(numericPriceString);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public String getPidFromDeepLink(String deepUrl) {
    try {
      URL url = new URL(deepUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty(
          "User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
      connection.setRequestProperty(
          "Accept",
          "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
      connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
      connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
      connection.setRequestProperty("Connection", "keep-alive");
      connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
      connection.setInstanceFollowRedirects(true);
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      String finalUrl = connection.getURL().toString();
      Pattern pattern = Pattern.compile("pid=([a-zA-Z0-9]+)");
      Matcher matcher = pattern.matcher(finalUrl);
      if (matcher.find()) {
        String pid = matcher.group(1);
        return pid;
      } else {
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private String fetchPrice(String pid) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("x-rapidapi-host", RAPIDAPI_HOST);
    headers.set("x-rapidapi-key", RAPIDAPI_KEY);
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<String> response =
        restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class, pid);
    return response.getBody();
  }

  public String extractPidFromURL(String deeplink) {
    Pattern pidPattern = Pattern.compile("pid=([A-Z0-9]+)");
    Matcher matcher = pidPattern.matcher(deeplink);
    if (matcher.find()) {
      return matcher.group(1); // Return the extracted PID
    }
    return null;
  }

  private String getFlipkartProductId(String input) {
    if (input.startsWith("https://www.") || input.startsWith("www.")) {
      return extractPidFromURL(input);
    }
    if (input.startsWith("https://dl.") || input.startsWith("dl.")) {
      return getPidFromDeepLink(input);
    }
    if (input.length() == 16 && input.matches("^[A-Za-z0-9]+$")) {
      return input;
    }
    return null;
  }

}
