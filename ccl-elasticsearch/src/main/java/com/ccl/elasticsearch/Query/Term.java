package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class Term {
    public static void main(String[] args) {
        termQuery();
    }

    /**
     * 默认的standard analyzer分词规则：<br>
     * 去掉大部分标点符号，并以此分割原词为多个词，把分分割后的词转为小写放入token组中。<br>
     * 对于not-analyzed的词，直接把原词放入token组中。<br>
     * termQuery的机制是：直接去匹配token。<br>
     * id=id2，默认分词，id2不分词。<br>
     * 以wwIF5-vP3J4l3GJ6VN3h为例：<br>
     * id是的token组是[wwif5,vp3j4l3gj6vn3h]<br>
     * id2的token组是[wwIF5-vP3J4l3GJ6VN3h]<br>
     * 可以预计以下结果：<br>
     * 1.termQuery("id", "wwif5")，有值。<br>
     * 2.termQuery("id", "vp3j4l3gj6vn3h")，有值。<br>
     * 3.termQuery("id2", "wwIF5-vP3J4l3GJ6VN3h")，有值。<br>
     */
    private static void termQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.termQuery("id", "wwif5");
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

