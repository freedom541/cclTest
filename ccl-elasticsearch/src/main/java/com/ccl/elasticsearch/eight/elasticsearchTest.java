package com.ccl.elasticsearch.eight;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


/**
 * Created by ccl on 16/11/18.
 */
public class ElasticsearchTest {
    public static Client getClient(){
        //使用本机做为节点
        //return getClient("49.50.39.218");
        return getClient("192.168.10.6");
    }

    public static Client getClient(String ipAddress){
        try {
            return TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ipAddress), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    //创建索引
    @Test
    public void testCreateIndex(){
        XContentBuilder mapping = null;
        try {
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("settings")
                    .field("number_of_shards", 1)//设置分片数量
                    .field("number_of_replicas", 0)//设置副本数量
                    .endObject()
                    .endObject()
                    .startObject()
                    .startObject("type")//type名称
                    .startObject("properties") //下面是设置文档列属性。
                    .startObject("type").field("type", "string").field("store", "yes").endObject()
                    .startObject("eventCount").field("type", "long").field("store", "yes").endObject()
                    .startObject("eventDate").field("type", "date").field("format", "dateOptionalTime").field("store", "yes").endObject()
                    .startObject("message").field("type", "string").field("index", "not_analyzed").field("store", "yes").endObject()
                    .endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CreateIndexRequestBuilder cirb = ElasticsearchTest.getClient()
                .admin()
                .indices()
                .prepareCreate("indexName")//index名称
                .setSource(mapping);

        CreateIndexResponse response = cirb.execute().actionGet();
        if (response.isAcknowledged()) {
            System.out.println("Index created.");
        } else {
            System.err.println("Index creation failed.");
        }
    }

    //增加文档
    @Test
    public void testAdd(){
        IndexResponse response = null;
        try {
            response = ElasticsearchTest.getClient()
                    .prepareIndex("indexName", "type", "1")
                    .setSource(//这里可以直接用json字符串
                            jsonBuilder().startObject()
                                    .field("type", "syslog")
                                    .field("eventCount", 1)
                                    .field("eventDate", new Date())
                                    .field("message", "secilog insert doc test")
                                    .endObject()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("index:"+response.getIndex()
                +" insert doc id:"+response.getId()
                +" result:"+response.isCreated());
    }

    //查询文档
    @Test
    public void testSearch(){
        GetResponse response = ElasticsearchTest.getClient().prepareGet("indexdemo", "typedemo", "AVh2T7jkDf7L-wBHmdEb").get();
        Map<String, Object> source = response.getSource();
        long version = response.getVersion();
        String indexName = response.getIndex();
        String type = response.getType();
        String id = response.getId();
    }

    //修改文档
    //修改文档有两种方式，一种是直接修改，另一种是如果文档不存在则插入存在则修改。
    //第一种代码
    @Test
    public void testUpdate(){
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("indexdemo");
        updateRequest.type("typedemo");
        updateRequest.id("AVh2T7jkDf7L-wBHmdEb");
        try {
            updateRequest.doc(jsonBuilder()
                    .startObject()
                    .field("name", "感冒 灵止咳糖浆")
                    .endObject());
            ElasticsearchTest.getClient().update(updateRequest).get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    //第二种代码
    @Test
    public void testUpdate2() throws IOException, ExecutionException, InterruptedException {
        IndexRequest indexRequest = new IndexRequest("indexdemo", "typedemo", "AVh2T7jkDf7L-wBHmdEb")
                .source(jsonBuilder()
                        .startObject()
                        .field("type", "syslog")
                        .field("eventCount", 2)
                        .field("eventDate", new Date())
                        .field("message", "secilog insert doc test")
                        .endObject());
        UpdateRequest updateRequest = new UpdateRequest("indexdemo", "typedemo", "AVh2T7jkDf7L-wBHmdEb")
                .doc(jsonBuilder()
                        .startObject()
                        .field("type", "fileeeee")
                        .endObject())
                .upsert(indexRequest);
        ElasticsearchTest.getClient().update(updateRequest).get();

    }

    //删除文档
    @Test
    public void testDelete(){
        DeleteResponse dresponse = ElasticsearchTest.getClient().prepareDelete("indexdemo", "typedemo", "AVh2T7jkDf7L-wBHmdEb").get();
        boolean isFound = dresponse.isFound(); //文档存在返回true,不存在返回false；
    }

    //删除索引
    @Test
    public void testDeleteIndex(){
        DeleteIndexRequest delete = new DeleteIndexRequest("feng");
        ElasticsearchTest.getClient().admin().indices().delete(delete);
    }



    @Test
    public void testWGUpdate(){
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("wg_monitor");
        updateRequest.type("ecs");
        updateRequest.id("i-2zedygvja2gvmuidrotx2016-11-30T01:00:00ZInternetRX");
        try {
            updateRequest.doc(jsonBuilder()
                    .startObject()
                    .field("instanceId", "i-bp1clmtcidgq5lplwbru")
                    .endObject());
            ElasticsearchTest.getClient().update(updateRequest).get();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testWGSearch(){
        GetResponse response = ElasticsearchTest.getClient().prepareGet("wg_monitor", "ecs", "d-2zei8fd8lio3b6uo4zl72016-11-30T11:00:00ZBPSTotal").get();
        Map<String, Object> source = response.getSource();
        long version = response.getVersion();
        String indexName = response.getIndex();
        String type = response.getType();
        String id = response.getId();
    }

    @Test
    public void testWGAdd(){
        IndexResponse response = null;
        try {
            response = ElasticsearchTest.getClient()
                    .prepareIndex("monitor", "ecs", "1")
                    .setSource(//这里可以直接用json字符串
                            jsonBuilder().startObject()
                                    .field("instanceId", "i-j6c8xqe2t2d7ucsh0nmu")
                                    .endObject()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("index:"+response.getIndex()
                +" insert doc id:"+response.getId()
                +" result:"+response.isCreated());
    }

}
