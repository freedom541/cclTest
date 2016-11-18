package com.ccl.elasticsearch.one;

/**
 * Created by ccl on 16/11/15.
 */

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * elasticsearch 相关操作工具类
 *
 * @author lzg
 * @date 2016年6月12日
 */
public class ESUtils {


    /**
     * es服务器的host
     */
    private static final String host = "49.50.39.218";

    /**
     * es服务器暴露给client的port
     */
    private static final int port = 9300;

    /**
     * jackson用于序列化操作的mapper
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 获得连接
     *
     * @return
     * @throws UnknownHostException
     */
    private static Client getClient() throws UnknownHostException {
        Client client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        return client;
    }

    /**
     * 创建商品索引
     *
     * @param goodsList
     *            商品dto的列表
     * @throws UnknownHostException
     * @throws JsonProcessingException
     */
    public static void createIndex(List<Goods> goodsList) throws UnknownHostException, JsonProcessingException {
        Client client = getClient();
        // 如果存在就先删除索引
        if (client.admin().indices().prepareExists("test_index").get().isExists()) {
            client.admin().indices().prepareDelete("test_index").get();
        }
        // 创建索引,并设置mapping.
        String mappingStr = "{ \"goods\" : { \"properties\": { \"id\": { \"type\": \"long\" }, \"name\": {\"type\": \"string\", \"analyzer\": \"ik_max_word\"}, \"regionIds\": {\"type\": \"string\",\"index\": \"not_analyzed\"}}}}";
        client.admin().indices().prepareCreate("test_index").addMapping("goods", mappingStr).get();

        // 批量处理request
        BulkRequestBuilder bulkRequest = client.prepareBulk();

        byte[] json;
        for (Goods goods : goodsList) {
            json = mapper.writeValueAsBytes(goods);
            bulkRequest.add(new IndexRequest("test_index", "goods", goods.getId() + "").source(json));
        }

        // 执行批量处理request
        BulkResponse bulkResponse = bulkRequest.get();

        // 处理错误信息
        if (bulkResponse.hasFailures()) {
            System.out.println("====================批量创建索引过程中出现错误 下面是错误信息==========================");
            long count = 0L;
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                System.out.println("发生错误的 索引id为 : "+bulkItemResponse.getId()+" ，错误信息为："+ bulkItemResponse.getFailureMessage());
                count++;
            }
            System.out.println("====================批量创建索引过程中出现错误 上面是错误信息 共有: "+count+" 条记录==========================");
        }

        client.close();
    }

    /**
     * 查询商品
     *
     * @param filter
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static List<Goods> search(GoodsFilter2ES filter)
            throws JsonParseException, JsonMappingException, IOException {
        Client client = getClient();
        QueryBuilder qb = new BoolQueryBuilder()
                .must(QueryBuilders.matchQuery("name",filter.getQueryStr()))
                .must(QueryBuilders.termQuery("regionIds", filter.getRegionId()));

        SearchResponse response = client.prepareSearch("test_index").setTypes("goods").setQuery(qb).execute()
                .actionGet();

        SearchHit[] hits = response.getHits().getHits();
        List<Goods> goodsIds = new ArrayList<Goods>();
        for (SearchHit hit : hits) {
            Goods goods = mapper.readValue(hit.getSourceAsString(), Goods.class);
            goodsIds.add(goods);
        }

        client.close();
        return goodsIds;
    }

    /**
     * 新增document
     *
     * @param index
     *            索引名称
     * @param type
     *            类型名称
     * @param goods
     *            商品dto
     * @throws UnknownHostException
     * @throws JsonProcessingException
     */
    public static void addDocument(String index, String type, Goods goods)
            throws UnknownHostException, JsonProcessingException {
        Client client = getClient();

        byte[] json = mapper.writeValueAsBytes(goods);

        client.prepareIndex(index, type, goods.getId() + "").setSource(json).get();

        client.close();
    }

    /**
     * 删除document
     *
     * @param index
     *            索引名称
     * @param type
     *            类型名称
     * @param goodsId
     *            要删除的商品id
     * @throws UnknownHostException
     */
    public static void deleteDocument(String index, String type, Long goodsId) throws UnknownHostException {
        Client client = getClient();

        client.prepareDelete(index, type, goodsId+"").get();

        client.close();
    }

    /**
     * 更新document
     *
     * @param index
     *            索引名称
     * @param type
     *            类型名称
     * @param goods
     *            商品dto
     * @throws JsonProcessingException
     * @throws UnknownHostException
     */
    public static void updateDocument(String index, String type, Goods goods)
            throws UnknownHostException, JsonProcessingException {
        //如果新增的时候index存在，就是更新操作
        addDocument(index, type, goods);
    }

}
