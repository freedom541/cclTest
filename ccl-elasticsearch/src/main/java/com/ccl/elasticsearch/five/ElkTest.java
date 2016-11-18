package com.ccl.elasticsearch.five;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.net.InetAddress;
import java.net.UnknownHostException;
public class ElkTest {
    //  private static final String CLUSTER_NAME = "cluster_name";
    public static final String CLUSTER_NAME = "elasticsearch"; //实例名称
    private static final String IP = "127.0.0.1";
    //private static final String IP = "192.168.0.29";
    private static final int PORT = 9300;  //端口
    //1.设置集群名称：默认是elasticsearch，并设置client.transport.sniff为true，使客户端嗅探整个集群状态，把集群中的其他机器IP加入到客户端中
    /*
    //对ES1.6有效
    private static Settings settings = ImmutableSettings
            .settingsBuilder()
            .put("cluster.name",CLUSTER_NAME)
            .put("client.transport.sniff", true)
            .build();
    */
    //对ES2.0有效
    private static Settings settings = Settings
            .settingsBuilder()
            .put("cluster.name",CLUSTER_NAME)
            .put("client.transport.sniff", true)
            .build();
    //创建私有对象
    private static TransportClient client;

    //反射机制创建单例的TransportClient对象  ES1.6版本
//    static {
//        try {
//            Class<?> clazz = Class.forName(TransportClient.class.getName());
//            Constructor<?> constructor = clazz.getDeclaredConstructor(Settings.class);
//            constructor.setAccessible(true);
//            client = (TransportClient) constructor.newInstance(settings);
//            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP), PORT));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //ES2.0版本
    static {
        try {
            client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP), PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //取得实例
    public static synchronized TransportClient getTransportClient(){
        return client;
    }

    //为集群添加新的节点
    public static synchronized void addNode(String name){
        try {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(name),9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //删除集群中的某个节点
    public static synchronized void removeNode(String name){
        try {
            client.removeTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(name),9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]){
        String index="logstash-2016.02.16";
        String type="logs";
        SearchResponse response=ElkTest.getTransportClient().prepareSearch(index)//设置要查询的索引(index)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTypes(type)//设置type, 这个在建立索引的时候同时设置了, 或者可以使用head工具查看
                .setQuery(QueryBuilders.matchQuery("message", "Accept")) //在这里"message"是要查询的field,"Accept"是要查询的内容
                .setFrom(0)
                .setSize(10)
                .setExplain(true)
                .execute()
                .actionGet();
        for(SearchHit hit:response.getHits()){
            System.out.println(hit.getSourceAsString());
        }
    }
}