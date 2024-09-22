package com.bargainbell.webscraper.service;

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
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class AmazonScrapingServiceByProxy {

    public String fetchAmazonProductPage(String url) {
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

    public Double getAmazonProductPrice(String url) {

        long startTime = System.currentTimeMillis();
        String pageContent = fetchAmazonProductPage(url);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Time taken to fetch Amazon Product Page thorugh proxy: " + timeTaken + " milliseconds");

        if (pageContent == null) {
            System.out.println("Failed to fetch page content for product code: " + url);
            return null;
        }

        try {
            // Parsing HTML with Jsoup
            Document doc = Jsoup.parse(pageContent);

            // Adjust the regex pattern for the specific Amazon price tag
            Elements priceElements = doc.select("span.a-price-whole");
            if (!priceElements.isEmpty()) {
                String price = priceElements.get(0).text().replace(",", "");
                return Double.parseDouble(price);
            } else {
                System.out.println("Price tag not found in the response.");
                return null;
            }

        } catch (Exception e) {
            System.out.println("Failed to fetch the price for Product Code: " + url);
            e.printStackTrace();
        }
        return null;
    }
}
