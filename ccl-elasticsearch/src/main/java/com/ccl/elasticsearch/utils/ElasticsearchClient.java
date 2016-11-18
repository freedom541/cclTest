package com.ccl.elasticsearch.utils;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * es操作客戶端
 *
 * @author dean
 * @date 2016/3/11.
 */
public class ElasticsearchClient {

    private static final String ID = "id";
    private static ElasticsearchClient singleInstance;

    private final TransportClient client;

    private ElasticsearchClient() {
        //建立客户端连接
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", Configs.getString("elasticsearch.cluster.name")).build();
        client = TransportClient.builder().settings(settings).build();
        String transports = Configs.getString("elasticsearch.transports");
        if (StringUtils.isNotBlank(transports)) {
            String[] strTrans = transports.split(",");
            for (int i = 0; i < strTrans.length; i++) {
                String[] tran = strTrans[i].split(":");
                if (tran.length > 1) {
                    client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(tran[0], Integer.parseInt(tran[1]))));
                } else {
                    client.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(tran[0], 9300)));
                }
            }
        }
    }

    public synchronized static ElasticsearchClient getConnection() {
        if (null == singleInstance) {
            singleInstance = new ElasticsearchClient();
        }
        return singleInstance;
    }

    public TransportClient getClient() {
        return client;
    }

    /**
     * 插入或者更新數據。
     * 沒指定id，則插入。指定id的情況下，如果該id數據存在，則更新；如果該id數據不存在，則插入。
     *
     * @param index
     * @param type
     * @param id
     * @param data
     * @return
     */
    public String index(String index, String type, String id, Map<String, Object> data) {
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index, type);
        if (StringUtils.isNotBlank(id)) {
            indexRequestBuilder.setId(id);
        }
        IndexResponse indexResponse = indexRequestBuilder.setSource(data).get();
        return indexResponse.getId();
    }

    /**
     * 插入或者更新數據。
     * 沒指定id，則插入。指定id的情況下，如果該id數據存在，則更新；如果該id數據不存在，則插入。
     *
     * @param index
     * @param type
     * @param id
     * @param data
     * @return
     */
    public String index(String index, String type, String id, String data) {
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index, type);
        if (StringUtils.isNotBlank(id)) {
            indexRequestBuilder.setId(id);
        }
        IndexResponse indexResponse = indexRequestBuilder.setSource(data).get();
        return indexResponse.getId();
    }

    /**
     * 刪除數據
     *
     * @param index
     * @param type
     * @param id
     */
    public void delete(String index, String type, String id) {
        client.prepareDelete(index, type, id).get();
    }

    /**
     * 獲取數據
     *
     * @param index
     * @param type
     * @param id
     * @return
     */
    public Map<String, Object> get(String index, String type, String id) {
        Map<String, Object> source = client.prepareGet(index, type, id).get().getSource();
        source.put(ID, id);
        return source;
    }

    /**
     * 分頁搜索
     *
     * @param index
     * @param type
     * @param query
     * @param page
     * @param size
     * @return
     */
    public Page<Map<String, Object>> search(String index, String type, String query, int page, int size) {
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index);
        SearchResponse searchResponse = searchRequestBuilder.setTypes(type).setFrom((page <= 0 ? 1 : page - 1) * size).setSize(size).setQuery(query).get();
        SearchHits hits = searchResponse.getHits();
        List<Map<String, Object>> content = new ArrayList<>();
        for (SearchHit searchHist : hits) {
            Map<String, Object> source = searchHist.getSource();
            source.put(ID, searchHist.getId());
            content.add(source);
        }
        Page<Map<String, Object>> result = new Page<>(content, page, size, null, hits.getTotalHits());
        return result;
    }
}
