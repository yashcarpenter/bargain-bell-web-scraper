package com.bargainbell.webscraper.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class MyntraScrapingServiceByProxy {

    public String fetchMyntraProductPage(String productCode) {
        String url = "https://www.myntra.com/" + productCode;
        HttpHost proxy = new HttpHost("brd.superproxy.io", 22225);
        String username = "brd-customer-hl_caf8d007-zone-test-country-in";
        String password = "bip9nff6wp63";

        try {
            // Create an SSLContext that trusts all certificates
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial((chain, authType) -> true)  // Trust all certs
                    .build();

            // Set up the SSL socket factory to bypass hostname verification
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultCredentialsProvider(getProxyCredentials(proxy, username, password))
                    .setRoutePlanner(new DefaultProxyRoutePlanner(proxy))
                    .build();

            // Set up the request with proper headers
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Safari/537.36");

            // Execute the request and get the response
            CloseableHttpResponse response = httpClient.execute(request);
            String content = EntityUtils.toString(response.getEntity());

            // Return the HTML content
            return content;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private BasicCredentialsProvider getProxyCredentials(HttpHost proxy, String username, String password) {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new org.apache.http.auth.AuthScope(proxy),
                new UsernamePasswordCredentials(username, password));
        return credsProvider;
    }

    public Double getMyntraProductPrice(String productCode) {
        String pageContent = fetchMyntraProductPage(productCode);
        if (pageContent == null) {
            System.out.println("Failed to fetch page content for product code: " + productCode);
            return null;
        }

        try {
            // Parsing HTML with Jsoup
            Document doc = Jsoup.parse(pageContent);

            // Extract the relevant script containing the product data
            Elements scripts = doc.select("script[type=application/ld+json]");
            for (Element script : scripts) {
                String jsonStr = script.html().trim();
                if (!jsonStr.isEmpty()) {
                    try {
                        JsonObject jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();

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
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch the price for Product Code: " + productCode);
            e.printStackTrace();
        }
        return null;
    }
}
