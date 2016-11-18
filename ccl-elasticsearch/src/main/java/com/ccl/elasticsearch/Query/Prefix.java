package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class Prefix {
    public static void main(String[] args) {
        prefixQuery();
    }

    /**
     * 以b3IAs@HhVBZPC6tVfyaK为例
     */
    private static void prefixQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.prefixQuery("id2",
                    "b3IAs@HhVBZPC6tVfyaK");
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

