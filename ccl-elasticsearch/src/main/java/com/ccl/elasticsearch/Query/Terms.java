package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class Terms {
    public static void main(String[] args) {
        termsQuery();
    }

    private static void termsQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            // termsQuery的第二个参数可以是数组，也可以是集合
            QueryBuilder query = QueryBuilders.termsQuery("age", new int[] {11, 16 });
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query).execute().actionGet();
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