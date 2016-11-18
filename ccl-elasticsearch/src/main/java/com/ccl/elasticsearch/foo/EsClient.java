package com.ccl.elasticsearch.foo;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.SortParseElement;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class EsClient {
	private Client client;

	// 初始化
	private void startUp() {
		if (client == null) {
			try {
				// 设置cluster.name和node.name
				Settings settings = Settings.settingsBuilder()
						.put("cluster.name", "test_cluster")
						.put("node.name", "test_node")
						.put("client.transport.sniff", true).build();
				// 连接到Client, 如果连接到一个 Elasticsearch 集群，构建器可以接受多个地址。（在本例中只有一个
				// localhost 节点。）
				client = TransportClient
						.builder()
						.settings(settings)
						.build()
						.addTransportAddress(
								new InetSocketTransportAddress(InetAddress
										.getByName("127.0.0.1"), 9300))
				// .addTransportAddress(
				// new InetSocketTransportAddress(InetAddress
				// .getByName("192.168.0.125"), 9300))//多集群地址
				;

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * PUT索引数据
	 */
	private IndexResponse putIndex() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "Lilei");
		map.put("age", "38");
		map.put("about", "Lilei and Hanmeimei.");

		// 使用UUID.randomUUID()生成一个ID
		IndexResponse response = client
				.prepareIndex("vinux", "employee", UUID.randomUUID().toString())
				.setSource(generateJson(map)).get();

		return response;
	}

	/**
	 * GET索引
	 *
	 * @return getResponse
	 */
	private GetResponse getIndex() {
		GetResponse getResponse = client.prepareGet("vinux", "employee", "1")
				.get();
		return getResponse;
	}

	/**
	 * SearchResponse对象查询
	 *
	 * @return
	 */
	private SearchResponse searchIndex() {
		SearchResponse searchResponse = client.prepareSearch("vinux")// 文档名称
				.setTypes("employee").get();// 类型
		return searchResponse;
	}

	/**
	 * QueryBuilder查询
	 *
	 * @return
	 */
	private SearchResponse QueryBuilder() {
		// 查询参数
		QueryBuilder matchQuery = QueryBuilders.matchQuery("name", "Lilei");
		QueryBuilder andQuery = QueryBuilders.fuzzyQuery("age", "100");// name
		// AND
		// age
		// 查询

		// 设置Client连接
		SearchResponse searchRequestBuilder = client.prepareSearch("vinux")
				.setTypes("employee").setQuery(matchQuery).setQuery(andQuery)
				.get();

		return searchRequestBuilder;
	}

	/**
	 * Scroll用法
	 *
	 * @return
	 */
	private SearchResponse scrollIndex() {
		QueryBuilder matchQuery = QueryBuilders.matchQuery("name", "Lilei");
		SearchResponse searchRequest = client.prepareSearch("vinux")
				.setTypes("employee")
				.addSort(SortParseElement.DOC_FIELD_NAME, SortOrder.ASC)// 排序
				.setScroll(new TimeValue(60000))// 设置滚动搜索的超时时间
				.setQuery(matchQuery).setSize(2).get();// size
		return searchRequest;
	}

	/**
	 * aggregations 聚合查询
	 * https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/java-search-aggs.html
	 * @return
	 */
	private SearchResponse aggregations() {
		SearchResponse searchRequest = client.prepareSearch()
				.setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(AggregationBuilders.terms("age").field("100")).addAggregation(AggregationBuilders.dateHistogram("age"))
				.get();
		//TODO
		return searchRequest;
	}

	/**
	 * count 数量查询
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private CountResponse countIndex(){
		CountResponse countResponse=client.prepareCount("vinux")
				.setTypes("employee")
				.setQuery(QueryBuilders.matchQuery("name", "Lilei")).get();
		return countResponse;
	}



	/**
	 * DELETE 索引
	 *
	 * @return delResponse
	 */
	private DeleteResponse delIndex() {
		DeleteResponse delResponse = client.prepareDelete("vinux", "employee",
				"AVW_erhIwjGNSfVFDfzf").get();
		return delResponse;
	}

	/**
	 * UPDATE
	 *
	 * 第一种方式：通过创建UpdateRequest对象，然后将其发送到客户端进行修改
	 */
	private UpdateResponse updateRequest() {
		UpdateRequest updateRequest = new UpdateRequest("vinux", "employee",
				"1");
		UpdateResponse updateResp = null;
		try {

			// 利用ES自带的XContentFactory.jsonBuilder()方法生成JSON数据
			updateRequest.doc(XContentFactory.jsonBuilder().startObject()
					.field("age", "10")// 将年龄改成10岁
					.endObject());

			// updateRequest.script(new Script("ctx._source.age = \"100\""));

			updateResp = client.update(updateRequest).get();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateResp;

	}

	/**
	 * UPDATE
	 *
	 * 第二种方式：利用prepareUpdate()方法。
	 *
	 * @return
	 */
	private UpdateResponse updateIndex() {
		UpdateResponse updateResponse = null;
		try {
			updateResponse = client
					.prepareUpdate("vinux", "employee", "1")
					.setDoc(XContentFactory.jsonBuilder().startObject()
							.field("age", "100")// 将年龄改成100岁
							.endObject()).get();

			// script方式创建文档
			// updateResponse = client
			// .prepareUpdate("vinux", "employee", "1")
			// .setScript(
			// new Script("ctx._source.age = \"100\"",
			// ScriptService.ScriptType.INLINE, null, null))
			// .get();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateResponse;
	}

	/**
	 * 第三种方式：使用upsert()方法，如果索引存在就修改，没有就add
	 *
	 * @return
	 */
	private UpdateResponse upsert() {
		UpdateResponse updateResponse = null;
		IndexRequest indexRequest = null;
		try {
			// 新建
			indexRequest = new IndexRequest("vinux", "employee", "7")
					.source(XContentFactory.jsonBuilder().startObject()
							.field("name", "zhanzhan").field("age", "40")
							.endObject());
			// 修改
			UpdateRequest updateRequest = new UpdateRequest("vinux",
					"employee", "7").doc(
					XContentFactory.jsonBuilder().startObject()
							.field("age", "1000").endObject()).upsert(
					indexRequest);// upsert()方法

			updateResponse = client.update(updateRequest).get();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return updateResponse;
	}

	/**
	 * MultiGetResponse多文档查询
	 *
	 * @return
	 */
	private MultiGetResponse multiIndexs() {
		MultiGetResponse multiGetResponse = null;
		multiGetResponse = client.prepareMultiGet()
				.add("vinux", "employee", "1")// 单个ID查询
				.add("vinux", "employee", "2", "5")// 多个ID查询
				.add("megacorp", "employee", "2")// 另一个索引
				.get();
		return multiGetResponse;
	}

	/**
	 * 利用ES自带的JSON生成器生成json数据
	 *
	 * @return
	 */
	private XContentBuilder generateJson(Map map) {
		XContentBuilder builder = null;
		try {
			builder = XContentFactory.jsonBuilder().startObject()
					.field("name", map.get("name"))
					.field("age", map.get("age"))
					.field("about", map.get("about")).endObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return builder;
	}

	// 关闭
	private void shutDown() {
		client.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EsClient esClient = new EsClient();
		esClient.startUp();
		// PUT
		// IndexResponse indexResponse = esClient.putIndex();
		// System.out.println("返回数据****************\n 索引名称INDEX_NAME: "
		// + indexResponse.getIndex() + "\n" + "索引类别INDEX_TYPE: "
		// + indexResponse.getType() + "\n" + "版本VERSION: "
		// + indexResponse.getVersion() + "\n" + "索引ID: "
		// + indexResponse.getId() + "\n" + "是否新建isCreated: "
		// + indexResponse.isCreated());

		// GET
		// GetResponse getResponse = esClient.getIndex();
		// System.out.println("返回数据：" + getResponse.getSourceAsString());

		// DEL
		// DeleteResponse delResponse = esClient.delIndex();
		// System.out.println("DEL:"+ delResponse.getVersion());

		// UpdateResponse updateResponse = esClient.updateIndex();
		// System.out.println("UPDATE:" + updateResponse.getVersion());

		// Upsert
		// UpdateResponse updateResponse = esClient.upsert();
		// System.out.println("UPDATE:" + updateResponse.getVersion());

		// Multi
		// MultiGetResponse multiGetResponse = esClient.multiIndexs();
		// for (MultiGetItemResponse multiGetItemResponse : multiGetResponse) {
		// GetResponse getResponse = multiGetItemResponse.getResponse();
		// System.out.println("多数据查询："+getResponse.getSourceAsString());
		// }

		// SearchResponse
		// SearchResponse searchResponse = esClient.searchIndex();
		// SearchHit[] searchHits = searchResponse.getHits().hits();
		// for (SearchHit searchHit : searchHits) {
		// System.out.println("SearchResponse:  "
		// + searchHit.getSourceAsString());//获取source数据
		// }

		// QueryBuilder
		// SearchResponse searchResponse = esClient.QueryBuilder();

//		SearchResponse searchResponse = esClient.scrollIndex();
//		SearchHit[] searchHits = searchResponse.getHits().hits();
//		for (SearchHit searchHit : searchHits) {
//			System.out.println("scroll查询结果:  " + searchHit.getSourceAsString());// 获取source数据
//		}
		CountResponse countResponse = esClient.countIndex();
		System.out.println("COUNT ="+ countResponse.getCount());

		esClient.shutDown();

	}

}
