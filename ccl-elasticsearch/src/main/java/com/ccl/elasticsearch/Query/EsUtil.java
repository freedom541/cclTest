package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EsUtil {
    private static Client client = null;

    public static Client getTransportClient() throws UnknownHostException {
        if (client == null
                || ((TransportClient) client).connectedNodes().isEmpty()) {
            synchronized (EsUtil.class) {
                if (client == null
                        || ((TransportClient) client).connectedNodes()
                        .isEmpty()) {
                    Settings settings = Settings.settingsBuilder()
                            .put("cluster.name", "elasticsearch")
                            .build();
                    client = TransportClient.builder().settings(settings)
                            .build()
                            .addTransportAddresses(
                                    new InetSocketTransportAddress(InetAddress
                                            .getByName("localhost"), 9300));
                }
            }
        }
        return client;
    }

    public static void close(Client client) {
        if (client != null) {
            client.close();
        }
    }
}
