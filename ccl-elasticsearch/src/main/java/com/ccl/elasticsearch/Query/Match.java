package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

public class Match {
    public static void main(String[] args) {
        matchAll();
        matchAllSize();
        matchQuery();
    }

    private static void matchAll() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.matchAllQuery();
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

    private static void matchAllSize() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.matchAllQuery();
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query).setSize(3).get();
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

    /**
     * 默认的standard analyzer分词规则：<br>
     * 去掉大部分标点符号，并以此分割原词为多个词，把分分割后的词转为小写放入token组中。<br>
     * 对于not-analyzed的词，直接把原词放入token组中。<br>
     * matchQuery的机制是：先检查字段类型是否是analyzed，如果是，则先分词，再去去匹配token；如果不是，则直接去匹配token。<br>
     * id=id2，默认分词，id2不分词。<br>
     * 以wwIF5-vP3J4l3GJ6VN3h为例：<br>
     * id是的token组是[wwif5,vp3j4l3gj6vn3h]<br>
     * id2的token组是[wwIF5-vP3J4l3GJ6VN3h]<br>
     * 可以预计以下结果：<br>
     * 1.matchQuery("id", "字符串")，"字符串"分词后有[wwif5,vp3j4l3gj6vn3h]其中之一时，有值。<br>
     * 如：wwIF5-vP3J4l3GJ6VN3h,wwif5-vp3j4l3gj6vn3h,wwIF5,wwif5,wwIF5-6666等等。<br>
     * 2.matchQuery("id2", "wwIF5-vP3J4l3GJ6VN3h")，有值。<br>
     * 特别说明：<br>
     * 在创建索引时，如果没有指定"index":"not_analyzed"<br>
     * 会使用默认的analyzer进行分词。当然你可以指定analyzer。<br>
     * 在浏览器中输入：<br>
     * http://localhost:9200/_analyze?pretty&analyzer=standard&text=J4Kz1%26L
     * bvjoQFE9gHC7H<br>
     * 可以看到J4Kz1&LbvjoQFE9gHC7H被分成了：j4kz1和lbvjoqfe9ghc7h<br>
     * %26是&符号，&?等符号是浏览器特殊符号，你懂的，可以用其它符号代替查看结果。<br>
     */
    private static void matchQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.matchQuery("id",
                    "wwif56,vp3j4l3gj6vn3h");
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
