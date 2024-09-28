package com.bargainbell.webscraper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Service
@Slf4j
public class ProxyChecker {

    private static final Logger logger = LoggerFactory.getLogger(ProxyChecker.class);

    public List<String> checkProxies() throws IOException {
        List<String> workingProxies = new ArrayList<>();

        String jsonFilePath = new ClassPathResource("proxies.json").getFile().getAbsolutePath();
        log.info("File path is: {}",jsonFilePath);
        String targetUrl = "https://www.google.com"; // The target URL to hit

        try {
            List<Map<String, Object>> proxyList = loadProxiesFromJson(jsonFilePath);

            for (Map<String, Object> proxy : proxyList) {
                try {
                    String ip = (String) proxy.get("ip");
                    String port = (String) proxy.get("port");
                    List<String> protocols = (List<String>) proxy.get("protocols");

                    // Use the proxy to hit the target URL
                    if (hitUrlUsingProxy(targetUrl, ip, Integer.parseInt(port), protocols)) {
                        workingProxies.add(ip + ":" + port);
                    }

                } catch (Exception e) {
                    logger.error("Error using proxy: {}", proxy, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error loading proxies from JSON", e);
        }

        return workingProxies; // Return the list of working proxies
    }

    private List<Map<String, Object>> loadProxiesFromJson(String jsonFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(jsonFilePath), new TypeReference<List<Map<String, Object>>>() {});
    }

    private boolean hitUrlUsingProxy(String targetUrl, String ip, int port, List<String> protocols) {
        try {
            Proxy proxy;
            if (protocols.contains("socks4") || protocols.contains("socks5")) {
                // Use SOCKS proxy
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(ip, port));
            } else {
                // Default to HTTP proxy
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            }

            URL url = new URL(targetUrl);
            URLConnection connection = url.openConnection(proxy);

            // Set a timeout for the connection (optional)
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Read response
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            logger.info("Successfully hit the URL using proxy {}:{} with protocol {}", ip, port, protocols);
            return true; // If successful, return true

        } catch (IOException e) {
            logger.error("Failed to hit the URL using proxy {}:{} with protocol {} - Error: {}", ip, port, protocols, e.getMessage());
            return false; // If failed, return false
        }
    }
}

