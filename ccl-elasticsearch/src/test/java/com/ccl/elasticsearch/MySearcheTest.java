package com.ccl.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ccl on 16/11/15.
 */
public class MySearcheTest {


    @Test
    public void search() {
        try {
            //创建客户端
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("49.50.39.218"), 9300));
            QueryBuilder qb = QueryBuilders.termQuery("value", "83320021");
            QueryBuilder qb2 = QueryBuilders.queryStringQuery("jjj");

            // 100|hits|per|shard|will|be|returned|for|each|scroll
            SearchResponse scrollResp = client.prepareSearch("monitor")
                    .setTypes("ecs")
                    .setSearchType(SearchType.SCAN)
                    .setScroll(new TimeValue(60000))
                    .setQuery(qb)
                    .setSize(100)
                    .execute()
                    .actionGet();
            // Scroll until no hits are returned
            while (true) {
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute()
                        .actionGet();
                for (SearchHit hit : scrollResp.getHits()) {
                    Iterator<Map.Entry<String, Object>> rpItor = hit.getSource().entrySet().iterator();
                    while (rpItor.hasNext()) {
                        Map.Entry<String, Object> rpEnt = rpItor.next();
                        System.out.println(rpEnt.getKey() + " : " + rpEnt.getValue());
                    }
                    System.out.println("------------------------------------------");
                }
                // Break condition: No hits are returned
                if (scrollResp.getHits().hits().length == 0) {
                    break;
                }
            }
            //结束
            client.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
