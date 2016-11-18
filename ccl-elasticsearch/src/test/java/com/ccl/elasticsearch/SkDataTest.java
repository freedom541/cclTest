package com.ccl.elasticsearch;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.stats.Stats;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ccl on 16/11/15.
 */
public class SkDataTest {
    @Test
    public void testsk() throws Exception{
        Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("49.50.39.218"), 9300));
        SearchResponse response = client.prepareSearch("monitor")
                .setTypes("ecs")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("timeStamp", "2016-11-14T")) // Query
                //.setPostFilter(QueryBuilders.rangeQuery("age").from(55).to(60)) // Filter
                //.setFrom(0).setSize(60)
                .setExplain(true).execute().actionGet();
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        for (int i = 0; i < hits.getHits().length; i++) {
            System.out.println(hits.getHits()[i].getSourceAsString());
        }

        client.close();
    }
    @Test
    public void tesGanymede() throws Exception{
        Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("49.50.39.218"), 9300));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.matchAllQuery());
        TermsBuilder gradeTermsBuilder = null;

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("monitor");

        List<String> instanceUid = Arrays.asList("i-230fn5lmi","i-25p590yer");

        boolQueryBuilder.must(QueryBuilders.termsQuery("instanceId", instanceUid));
        //统计信息
        gradeTermsBuilder = AggregationBuilders.terms("gradeAgg").size(instanceUid.size()).field("instanceId");
        TermsBuilder keyTermsBuilder = AggregationBuilders.terms("gradeKey").field("key").include(new String[]{"CPU","InternetTX","InternetRX","EipTX","EipRX","BPSRead","BPSWrite"});
        MetricsAggregationBuilder aggregation = AggregationBuilders.stats("MySQL_NetworkTrafficStatus").field("value");
        gradeTermsBuilder.subAggregation(keyTermsBuilder.subAggregation(aggregation));

        boolQueryBuilder.filter(QueryBuilders.rangeQuery("timeStamp").gte("2016-08-14"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("timeStamp").lte("2016-11-16"));

        SearchResponse response = searchRequestBuilder.setTypes("ecs").setQuery(boolQueryBuilder).addAggregation(gradeTermsBuilder).get();
        Terms terms = response.getAggregations().get("gradeAgg");

        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        System.out.println(hits.getHits().length);
        for (int i = 0; i < hits.getHits().length; i++) {
            System.out.println("i=" +i + "     " + hits.getHits()[i].getSourceAsString());
        }
        System.out.println("=========================================");
        for (Terms.Bucket instanceTerm : terms.getBuckets()){
            String filedName = instanceTerm.getKeyAsString(); //實例Id
            Terms keyTerms = instanceTerm.getAggregations().get("gradeKey");
            for (Terms.Bucket keyTerm : keyTerms.getBuckets()) {
                try {
                    String keyName = keyTerm.getKeyAsString(); //key
                    Stats stats = keyTerm.getAggregations().get("MySQL_NetworkTrafficStatus");
                    System.out.println(keyName + "========" + stats.getAvg() + ","+ stats.getMin() + ","+ stats.getMax());
                } catch (Exception e) {

                }
            }
        }

        client.close();
    }
    @Test
    public void tesGanymede2() throws Exception{
        Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("49.50.39.218"), 9300));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.matchAllQuery());

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("monitor");

        List<String> instanceUid = Arrays.asList("i-bp1ipvbenil9ol7wut10","i-bp1h41nalpspgahth94h","i-bp165rhsn1778uns2qtq","i-bp17yehfzdriu8vyk9kb");

        boolQueryBuilder.must(QueryBuilders.termsQuery("instanceId", instanceUid));

        boolQueryBuilder.filter(QueryBuilders.rangeQuery("timeStamp").gte("2016-11-01"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("timeStamp").lte("2016-11-02"));

        SearchResponse response = searchRequestBuilder.setTypes("ecs").setQuery(boolQueryBuilder).setSize(1000).get();

        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        System.out.println(hits.getHits().length);
        for (int i = 0; i < hits.getHits().length; i++) {
            System.out.println("i=" +i + "     " + hits.getHits()[i].getSourceAsString());
        }

        client.close();
    }
}