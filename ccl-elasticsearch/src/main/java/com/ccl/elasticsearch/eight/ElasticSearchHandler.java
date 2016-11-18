package com.ccl.elasticsearch.eight;

/**
 * Created by ccl on 16/11/18.
 */
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchHandler {

    private Client client;

    public ElasticSearchHandler(){
        //使用本机做为节点
        this("49.50.39.218");
    }

    public ElasticSearchHandler(String ipAddress){
        //集群连接超时设置
        /*
              Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.ping_timeout", "10s").build();
            client = new TransportClient(settings);
         */
        try {
            client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ipAddress), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    /**
     * 建立索引,索引建立好之后,会在elasticsearch-0.20.6\data\elasticsearch\nodes\0创建所以你看
     * @param indexname  为索引库名，一个es集群中可以有多个索引库。 名称必须为小写
     * @param jsondata  Type为索引类型，是用来区分同索引库下不同类型的数据的，一个索引库下可以有多个索引类型。
     * @param jsondata     json格式的数据集合
     *
     * @return
     */
    public void createIndexResponse(String indexname, String type, List<String> jsondata){
        //创建索引库 需要注意的是.setRefresh(true)这里一定要设置,否则第一次建立索引查找不到数据
        IndexRequestBuilder requestBuilder = client.prepareIndex(indexname, type).setRefresh(true);
        for(int i=0; i<jsondata.size(); i++){
            requestBuilder.setSource(jsondata.get(i)).execute().actionGet();
        }

    }

    /**
     * 创建索引
     * @param type
     * @param jsondata
     * @return
     */
    public IndexResponse createIndexResponse(String indexname, String type,String jsondata){
        IndexResponse response = client.prepareIndex(indexname, type)
                .setSource(jsondata)
                .execute()
                .actionGet();
        return response;
    }

    /**
     * 执行搜索
     * @param queryBuilder
     * @param indexname
     * @param type
     * @return
     */
    public List<Medicine>  searcher(QueryBuilder queryBuilder, String indexname, String type){
        List<Medicine> list = new ArrayList<Medicine>();
        SearchResponse searchResponse = client.prepareSearch(indexname).setTypes(type)
                .setQuery(queryBuilder)
                .execute()
                .actionGet();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询到记录数=" + hits.getTotalHits());
        SearchHit[] searchHists = hits.getHits();
        if(searchHists.length>0){
            for(SearchHit hit:searchHists){
                Integer id = (Integer)hit.getSource().get("id");
                String name =  (String) hit.getSource().get("name");
                String function =  (String) hit.getSource().get("funciton");
                list.add(new Medicine(id, name, function));
            }
        }
        return list;
    }


    public static void main(String[] args) {
        ElasticSearchHandler esHandler = new ElasticSearchHandler();
        List<String> jsondata = DataFactory.getInitJsonData();
        String indexname = "indexdemo";
        String type = "typedemo";
        esHandler.createIndexResponse(indexname, type, jsondata);
        //查询条件
        QueryBuilder queryBuilder = QueryBuilders.termQuery("name", "粒");
        /*QueryBuilder queryBuilder = QueryBuilders.boolQuery()
          .must(QueryBuilders.termQuery("id", 1));*/
        List<Medicine> result = esHandler.searcher(queryBuilder, indexname, type);
        for(int i=0; i<result.size(); i++){
            Medicine medicine = result.get(i);
            System.out.println("(" + medicine.getId() + ")药品名称:" +medicine.getName() + "\t\t" + medicine.getFunction());
        }
    }

}
