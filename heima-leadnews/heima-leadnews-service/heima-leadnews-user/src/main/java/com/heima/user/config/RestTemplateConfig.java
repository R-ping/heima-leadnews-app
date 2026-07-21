package com.heima.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * <p>
 * 开发环境关闭 SSL 证书校验，解决 "PKIX path building failed" 错误。
 * 生产环境请正确导入 CA 证书，不要使用此配置。
 */
@Configuration
public class RestTemplateConfig {

//    static {
//        disableSslVerification();
//    }

    /**
     * 关闭 SSL 证书验证（仅用于开发环境）
     */
//    private static void disableSslVerification() {
//        try {
//            TrustManager[] trustAllCerts = new TrustManager[]{
//                new X509TrustManager() {
//                    public X509Certificate[] getAcceptedIssuers() {
//                        return new X509Certificate[0];
//                    }
//                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                    }
//                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                    }
//                }
//            };
//
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//            HostnameVerifier allHostsValid = (hostname, session) -> true;
//            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//        } catch (Exception e) {
//            throw new RuntimeException("关闭 SSL 验证失败", e);
//        }
//    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }
}