package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;

public class Avg {
    public static void main(String[] args) {
        avgQuery();
    }

    private static void avgQuery() {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            QueryBuilder query = QueryBuilders.matchQuery("name", "张三");
            SearchResponse response = client.prepareSearch("product")
                    .setTypes("user").setQuery(query)
                    .addAggregation(
                            AggregationBuilders.avg("age_avg").field("age"))
                    .addAggregation(
                            AggregationBuilders.sum("salary_sum").field(
                                    "salary"))
                    .execute().actionGet();
            SearchHits hits = response.getHits();
            InternalAvg agg = response.getAggregations().get("age_avg");
            System.out.println(agg.getName() + "\t" + agg.getValue());
            InternalSum agg2 = response.getAggregations().get("salary_sum");
            System.out.println(agg2.getName() + "\t" + agg2.getValue());
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

