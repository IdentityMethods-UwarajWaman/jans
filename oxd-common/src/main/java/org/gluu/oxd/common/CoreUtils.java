/**
 * All rights reserved -- Copyright 2015 Gluu Inc.
 */
package org.gluu.oxd.common;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.gluu.oxd.common.proxy.ProxyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Core utility class.
 *
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 27/07/2013
 */
public class CoreUtils {

    public static final String DOC_URL = "https://gluu.org/docs/oxd";

    public static boolean isExpired(Date expiredAt) {
        return expiredAt != null && expiredAt.before(new Date());
    }


    /**
     * UTF-8 encoding string
     */
    public static final String UTF8 = "UTF-8";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CoreUtils.class);

    public static final int COMMAND_STR_LENGTH_SIZE = 4;
    public static final int BUFFER_SIZE = 8192;

    /**
     * Avoid instance creation.
     */
    private CoreUtils() {
    }

    public static ScheduledExecutorService createExecutor() {
        return Executors.newSingleThreadScheduledExecutor(daemonThreadFactory());
    }

    public static ThreadFactory daemonThreadFactory() {
        return new ThreadFactory() {
            public Thread newThread(Runnable p_r) {
                Thread thread = new Thread(p_r);
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    public static void sleep(int i) {
        try {
            Thread.sleep(i * 1000);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static long parseSilently(String p_str) {
        try {
            return Long.parseLong(p_str);
        } catch (Exception e) {
            return -1;
        }
    }

    public static String normalizeLengthPrefixString(int p_length) {
        if (p_length <= 0) {
            throw new IllegalArgumentException("Length must be more than zero.");
        }
        final String s = Integer.toString(p_length);
        final StringBuilder sb = new StringBuilder(s);
        final int sbLength = sb.length();
        if (sbLength < COMMAND_STR_LENGTH_SIZE) {
            for (int i = sbLength; i < COMMAND_STR_LENGTH_SIZE; i++) {
                sb.insert(0, '0');
            }
        }
        if (sb.length() != COMMAND_STR_LENGTH_SIZE) {
            throw new IllegalArgumentException("Normalized length size must be exactly: " + COMMAND_STR_LENGTH_SIZE);
        }
        return sb.toString();
    }

    public static ReadResult readCommand(String p_leftString, BufferedReader p_reader) throws IOException {
        int commandSize = -1;
        final StringBuilder storage = new StringBuilder(p_leftString != null ? p_leftString : "");
        while (true) {
            LOG.trace("commandSize: {}, stringStorage: {}", commandSize, storage.toString());

            final char[] buffer = new char[BUFFER_SIZE];
            final int readCount = p_reader.read(buffer, 0, BUFFER_SIZE);
            if (readCount == -1) {
                LOG.trace("End of stream. Quit.");
                return null;
            }

            storage.append(buffer, 0, readCount);

            final int storageLength = storage.length();
            if (commandSize == -1 && storageLength >= CoreUtils.COMMAND_STR_LENGTH_SIZE) {
                final String sizeString = storage.substring(0, CoreUtils.COMMAND_STR_LENGTH_SIZE);
                commandSize = (int) CoreUtils.parseSilently(sizeString);
                LOG.trace("Parsed sizeString: {}, commandSize: {}", sizeString, commandSize);

                if (commandSize == -1) {
                    LOG.trace("Unable to identify command size. Quit.");
                    return null;
                }
            }

            final int totalSize = commandSize + CoreUtils.COMMAND_STR_LENGTH_SIZE;
            if (commandSize != -1 && storageLength >= totalSize) {
                final String commandAsString = storage.substring(
                        CoreUtils.COMMAND_STR_LENGTH_SIZE, totalSize);

                String leftString = "";
                if (storageLength > (totalSize + 1)) {
                    storage.substring(totalSize + 1);
                }
                final ReadResult result = new ReadResult(commandAsString, leftString);
                LOG.trace("Read result: {}", result);
                return result;
            }
        }
    }

    public static boolean allNotBlank(String... p_strings) {
        if (p_strings != null && p_strings.length > 0) {
            for (String s : p_strings) {
                if (org.apache.commons.lang.StringUtils.isBlank(s)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param pathToKeyStore path to key store, e.g. D:/Development/gluu_conf/etc/certs/DA855F9895A1CA3B9E7D4BF5-java.jks
     * @param password       key store password
     * @return http client
     * @throws Exception
     */


    public static HttpClient createHttpClientWithKeyStore(File pathToKeyStore, String password, ProxyConfiguration proxyConfiguration) throws Exception {

        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(pathToKeyStore, password.toCharArray())
                .build();

        SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(
                sslcontext, SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        /*Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConSocFactory)
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);*/

        return createClient(sslConSocFactory, proxyConfiguration);
    }

    public static HttpClient createHttpClientTrustAll(ProxyConfiguration proxyConfiguration) throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException {

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).build();
        SSLConnectionSocketFactory sslContextFactory = new SSLConnectionSocketFactory(sslContext);

        return createClient(sslContextFactory, proxyConfiguration);
    }

    public static HttpClient createClient(SSLConnectionSocketFactory connectionFactory, ProxyConfiguration proxyConfiguration) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        HttpClientBuilder httClientBuilder = HttpClients.custom();

        if (connectionFactory != null) {
            httClientBuilder = httClientBuilder.setSSLSocketFactory(connectionFactory);
        }

        if (proxyConfiguration != null && !Strings.isNullOrEmpty(proxyConfiguration.getHost())) {
            HttpHost proxyhost = null;
            if (isSafePort(proxyConfiguration.getPort()) && !Strings.isNullOrEmpty(proxyConfiguration.getProtocol())) {
                proxyhost = new HttpHost(proxyConfiguration.getHost(), proxyConfiguration.getPort(), proxyConfiguration.getProtocol());
            } else if (isSafePort(proxyConfiguration.getPort())) {
                proxyhost = new HttpHost(proxyConfiguration.getHost(), proxyConfiguration.getPort());
            } else {
                proxyhost = new HttpHost(proxyConfiguration.getHost());
            }
            //HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyhost);
            //httClientBuilder = httClientBuilder.setRoutePlanner(routePlanner);
            httClientBuilder = httClientBuilder.setProxy(proxyhost);
        }

        CloseableHttpClient httpClient = httClientBuilder
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setConnectionManager(cm).build();

        cm.setMaxTotal(200); // Increase max total connection to 200
        cm.setDefaultMaxPerRoute(20); // Increase default max connection per route to 20

        return httpClient;
    }

    public static String secureRandomString() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException, MalformedURLException {
        return splitQuery(new URL(url));
    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new LinkedHashMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return queryPairs;
    }

    public static String cleanUpLog(String log) {
        try {
            // remove `client_secret` from logs
            final int index = StringUtils.indexOf(log, "client_secret");
            if (index != -1) {
                final int commaIndex = StringUtils.indexOf(log, ",", index + 1);
                return log.substring(0, index - 1) + log.substring(commaIndex + 1, log.length());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return log;
    }

    private static boolean isSafePort(Integer input) {
        return input != null && input > 0;
    }
}
