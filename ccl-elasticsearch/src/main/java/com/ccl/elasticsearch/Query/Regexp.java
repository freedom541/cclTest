package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class Regexp {
    public static void main(String[] args) {
        regexpQuery();
    }

    private static void regexpQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.regexpQuery("id2",
                    "[a-z,A-Z]{5}\\+.*");
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query).setFrom(0).setSize(50)
                    .execute().actionGet();
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
