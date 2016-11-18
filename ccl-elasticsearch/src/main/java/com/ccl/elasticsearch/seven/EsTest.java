package com.ccl.elasticsearch.seven;

/**
 * Created by ccl on 16/11/18.
 */
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by wangnian on 2016/5/29.
 */
public class EsTest {

    Client client = getClient("49.50.39.218");

    //创建客户端
    public Client getClient(String host) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), 9300));
            return client;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return null;
    }

    /**
     * 将对象通过jackson.databind转换成byte[]
     * 注意一下date类型需要格式化处理  默认是 时间戳
     *
     * @return
     */
    public byte[] convertByteArray(Object obj) {
        ObjectMapper mapper = new ObjectMapper(); // create once, reuse
        try {
            byte[] json = mapper.writeValueAsBytes(obj);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将对象通过JSONtoString转换成JSON字符串
     * 使用fastjson 格式化注解  在属性上加入 @JSONField(format="yyyy-MM-dd HH:mm:ss")
     *
     * @return
     */
    public String jsonStr(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 增删改查
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("name", "wangnian11");
        json.put("postDate", new Date());
        json.put("message", "trying out Elasticsearch");

        User user = new User();
        user.setId(2);
        user.setName("更新第一条");
        user.setPostDate(new Date());
        user.setMessage("这是一个测试的数据");

        IndexResponse response = client.prepareIndex("index", "user", String.valueOf(user.getId()))//参数说明： 索引，类型 ，_id
                .setSource(jsonStr(user))//setSource可以传以上map string  byte[] 几种方式
                .get();
        boolean created = response.isCreated();
        System.out.println("创建一条记录:" + created);

        //删除_id为1的类型
        DeleteResponse response2 = client.prepareDelete("index", "user", "2").get();
        System.out.println("删除一条数据：" + response2.isFound());

        //更新
        UpdateResponse updateResponse = client.prepareUpdate("index", "user", "1").setDoc(jsonBuilder()
                .startObject()
                .field("name", "王念")
                .endObject())
                .get();
        System.out.println("更新一条数据:" + updateResponse.isCreated());


        //获取_id为1的类型
        GetResponse response1 = client.prepareGet("index", "user", "1").get();
        response1.getSourceAsMap();//获取值转换成map
        System.out.println("查询一条数据:" + JSON.toJSON(response1.getSourceAsMap()));
    }

    @Test
    public void test2() {
        //查询多个id的值
        MultiGetResponse multiGetItemResponses = client.prepareMultiGet()
                .add("index", "user", "1")
                .add("index", "user", "2", "3", "4")
                .get();
        for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
            GetResponse response = itemResponse.getResponse();
            if (response.isExists()) {
                String json = response.getSourceAsString();
                System.out.println(json);
            }
        }
        //搜索
        SearchResponse response = client.prepareSearch("index")//可以同时搜索多个索引prepareSearch("index","index2")
                .setTypes("user")//可以同时搜索多个类型
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("name", "张三"))                 // Query
                .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
                .setFrom(0).setSize(2).setExplain(true)
                .execute()
                .actionGet();
        forSearchResponse(response);
        System.out.println("总共查询到有：" + response.getHits().getTotalHits());


        //多查询结果
        SearchRequestBuilder srb1 = client
                .prepareSearch().setQuery(QueryBuilders.queryStringQuery("张三")).setSize(1);
        SearchRequestBuilder srb2 = client
                .prepareSearch().setQuery(QueryBuilders.matchQuery("name", "张三")).setSize(1);

        MultiSearchResponse sr = client.prepareMultiSearch()
                .add(srb1)
                .add(srb2)
                .execute().actionGet();

        long nbHits = 0;
        for (MultiSearchResponse.Item item : sr.getResponses()) {
            SearchResponse response1 = item.getResponse();
            forSearchResponse(response1);
            nbHits += response1.getHits().getTotalHits();
        }
        System.out.println("多查询总共查询到有：" + nbHits);
    }

    public void forSearchResponse(SearchResponse response) {
        for (SearchHit hit1 : response.getHits()) {
            Map<String, Object> source1 = hit1.getSource();
            if (!source1.isEmpty()) {
                for (Iterator<Map.Entry<String, Object>> it = source1.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, Object> entry = it.next();
                    System.out.println(entry.getKey() + "=======" + entry.getValue());
                }
            }
        }
    }
}
