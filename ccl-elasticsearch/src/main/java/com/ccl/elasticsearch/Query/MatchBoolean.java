package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class MatchBoolean {
    public static void main(String[] args) {
        matchBoolean1();
        matchBoolean2();
    }

    private static void matchBoolean1() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery("age", 16))
                    .must(QueryBuilders.matchQuery("name", "李四"));
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query).get();
            SearchHits hits = response.getHits();
            long total = hits.getTotalHits();
            System.out.println("total:" + total);
            int len = hits.getHits().length;
            System.out.println("len:" + len);
            Printer.print(hits);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EsUtil.close(client);
        }
    }

    private static void matchBoolean2() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("age", 16))
                    .must(QueryBuilders.matchQuery("name", "李四"));
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query).get();
            SearchHits hits = response.getHits();
            long total = hits.getTotalHits();
            System.out.println("total:" + total);
            int len = hits.getHits().length;
            System.out.println("len:" + len);
            Printer.print(hits);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EsUtil.close(client);
        }
    }
}
