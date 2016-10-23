package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.google.android.gcm.server.Sender;

public class ProxySender extends Sender {
	private String proxyIP;
	private Integer proxyPort;
    public ProxySender(String key, String proxyIP, Integer proxyPort) {
        super(key);
        this.proxyIP = proxyIP;
        this.proxyPort = proxyPort;
    }
    @Override
    protected HttpURLConnection getConnection(String url) throws IOException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIP, proxyPort));
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection(proxy);
        return conn;
    }
}