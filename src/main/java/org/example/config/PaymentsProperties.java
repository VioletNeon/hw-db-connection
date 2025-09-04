package org.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payments")
public class PaymentsProperties {
    private String productsBaseUrl;
    private Http http = new Http();

    public String getProductsBaseUrl() { return productsBaseUrl; }
    public void setProductsBaseUrl(String productsBaseUrl) { this.productsBaseUrl = productsBaseUrl; }

    public Http getHttp() { return http; }

    public static class Http {
        private int connectTimeoutMs = 1500;
        private int readTimeoutMs = 2500;

        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    }
}
