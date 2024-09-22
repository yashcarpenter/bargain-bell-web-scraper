package com.bargainbell.webscraper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FlipkartApiService {

  @Autowired
  private RestTemplate restTemplate;

  private static final String API_URL = "https://real-time-flipkart-api.p.rapidapi.com/product-details?pid={pid}";
  private static final String RAPIDAPI_HOST = "real-time-flipkart-api.p.rapidapi.com";
  private static final String RAPIDAPI_KEY = "621d292537mshc52561248cd1788p18bd91jsn0c3f456b3009";

  private final String PROXY_HOST = "brd.superproxy.io";
  private final int PROXY_PORT = 22225;
  private final String PROXY_USERNAME = "brd-customer-hl_caf8d007-zone-test-country-in";
  private final String PROXY_PASSWORD = "bip9nff6wp63";

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
      String finalUrl = fetchRedirectedUrl(deepUrl);
      return extractPidFromUrl(finalUrl);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private String fetchRedirectedUrl(String deepUrl) throws Exception {
    HttpGet request = new HttpGet(deepUrl);
    request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");
    request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
    request.setHeader("Accept-Encoding", "gzip, deflate, br");
    request.setHeader("Accept-Language", "en-US,en;q=0.5");

    try (CloseableHttpClient httpClient = createHttpClient();
         CloseableHttpResponse response = httpClient.execute(request)) {

      // Check the response status code
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new Exception("Failed to fetch URL: " + response.getStatusLine().getStatusCode());
      }

      // Print the response body for debugging
      String responseBody = EntityUtils.toString(response.getEntity());
      System.out.println("Response Body: " + responseBody);

      // Check for the Location header
      org.apache.http.Header locationHeader = response.getFirstHeader("Location");
      if (locationHeader != null) {
        return locationHeader.getValue();
      } else {
        throw new Exception("No Location header found in the response.");
      }
    }
  }



  private CloseableHttpClient createHttpClient() throws Exception {
    HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
    SSLContext sslContext = SSLContextBuilder.create()
            .loadTrustMaterial((chain, authType) -> true)  // Trust all certs
            .build();

    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
            new org.apache.http.auth.AuthScope(proxy),
            new UsernamePasswordCredentials(PROXY_USERNAME, PROXY_PASSWORD)
    );

    return HttpClients.custom()
            .setSSLContext(sslContext)
            .setDefaultCredentialsProvider(credsProvider)
            .setRoutePlanner(new DefaultProxyRoutePlanner(proxy))
            .build();
  }

  private String extractPidFromUrl(String finalUrl) {
    Pattern pattern = Pattern.compile("pid=([a-zA-Z0-9]+)");
    Matcher matcher = pattern.matcher(finalUrl);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
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

  private String getFlipkartProductId(String input) {
    if (input.startsWith("https://www.") || input.startsWith("www.")) {
      return extractPidFromUrl(input);
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
