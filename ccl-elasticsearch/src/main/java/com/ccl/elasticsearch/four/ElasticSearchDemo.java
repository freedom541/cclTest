package com.ccl.elasticsearch.four;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by ccl on 16/11/16.
 */
public class ElasticSearchDemo {
    Client client = null;

    /**
     * 1.获取client实例，连接本地9300端口
     */
    {
        //连接单台机器，注意ip和端口号，不能写错
        try {
            client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("49.50.39.218"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    /**
     * 2.生成一个索引。这里用Map来保存json数据，然后插入到index为“twitter”的索引里面，其document为“tweet”，id为“1”。当然，生成json数据的方法很多，朋友们可以查看相关api。
     */
    public void generateIndex() {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("user", "kimchy");
        json.put("postDate", new Date());
        json.put("message", "trying out Elastic Search");

        IndexResponse response = this.client
                .prepareIndex("twitter", "tweet", "1").setSource(json)
                .execute().actionGet();
    }

    /**
     * 3.查询某个索引 ，这个一看就明白。
     */
    public void getIndex() {
        GetResponse response = client.prepareGet("twitter", "tweet", "1")
                .execute().actionGet();
        Map<String, Object> rpMap = response.getSource();
        if (rpMap == null) {
            System.out.println("empty");
            return;
        }
        Iterator<Map.Entry<String, Object>> rpItor = rpMap.entrySet().iterator();
        while (rpItor.hasNext()) {
            Map.Entry<String, Object> rpEnt = rpItor.next();
            System.out.println(rpEnt.getKey() + " : " + rpEnt.getValue());
        }
    }

    /**
     * 4. 搜索，创建一个termQuery查询，该查询要求全部匹配才会出结果，如果只要包含关键字里面一部分，可以创建fieldQuery。
     */
    public void searchIndex() {

        QueryBuilder qb = QueryBuilders.termQuery("user", "kimchy");
        SearchResponse scrollResp = client.prepareSearch("twitter")
                .setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
        //Scroll until no hits are returned
        while (true) {
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
            for (SearchHit hit : scrollResp.getHits()) {
                Iterator<Map.Entry<String, Object>> rpItor = hit.getSource().entrySet().iterator();
                while (rpItor.hasNext()) {
                    Map.Entry<String, Object> rpEnt = rpItor.next();
                    System.out.println(rpEnt.getKey() + " : " + rpEnt.getValue());
                }
            }
            //Break condition: No hits are returned
            if (scrollResp.getHits().hits().length == 0) {
                break;
            }
        }
    }

    /**
     * 5.删除，删除的时候要指定Id的，这里指定id为1.
     */
    public void deleteIndex() {
        DeleteResponse response = client.prepareDelete("twitter", "tweet", "1")
                .execute()
                .actionGet();
    }

    /**
     * 6.操作完毕后别忘记最后一步：关闭client连接。
     */
    public void closeClient() {
        client.close();
    }


}
