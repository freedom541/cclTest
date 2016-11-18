package com.ccl.elasticsearch.six;

/**
 * Created by ccl on 16/11/16.
 */

import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author whx 下午6:52:53
 * @desc elasticsearch的java工具类
 */
public class ElasticsearchTools {

    private static String ip = "49.50.39.218";
    private static int port = 9300;


    /**
     *@Title: addIndex
     *@Description: TODO  单个索引增加
     *@param @param object  要增加的数据
     *@param @param index   索引，类似数据库
     *@param @param type    类型，类似表
     *@param @param id      id
     *@return void
     *@throws
     */
    public static void addDocument(JSONObject object, String index, String type, String id) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
            IndexResponse response = client.prepareIndex(index, type, id).setSource(object).get();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     *@Title: getIndex
     *@Description: TODO 获取某条信息
     *@param @param index
     *@param @param type
     *@param @param id
     *@param @return
     *@return Map<String,Object>
     *@throws
     */
    public static Map<String, Object> getDocument(String index, String type, String id) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
            GetResponse response = client.prepareGet(index, type, id).get();
            Map<String, Object> map = response.getSource();
            return map;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    /**
     *@Title: delDocument
     *@Description: TODO 删除某条信息
     *@param @param index
     *@param @param type
     *@param @param id
     *@return void
     *@throws
     */
    public static void delDocument(String index, String type, String id) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
            DeleteResponse response = client.prepareDelete(index, type, id).get();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     *@Title: delDocument
     *@Description: TODO 更新某条信息 ,如果改动很多，直接用新增的也可以，只要id相同即可
     *@param @param index
     *@param @param type
     *@param @param id
     *@return void
     *@throws
     */
    public static void updateDocument(String index, String type, String id, String key, String value) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
            UpdateRequest updateRequest = new UpdateRequest(index, type, id);
            updateRequest.doc(XContentFactory.jsonBuilder().startObject().field(key, value).endObject());
            client.update(updateRequest).get();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    }

    /**
     *@Title: getDocuments
     *@Description: TODO 通过多个id，去查询一个list,暂时没有太大的用处 ，需要的话 请自己修改入参 调整
     *@param @param index
     *@param @param type
     *@param @param id
     *@param @return
     *@return List<Map<String,Object>>
     *@throws
     */
    public static List<Map<String, Object>> getDocuments(String index, String type, String id1, String id2) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
            //client.prepareMultiGet().add("twitter", "tweet", "1").add("twitter", "tweet", "2", "3", "4").add("another", "type", "foo").get();
            MultiGetResponse multiGetItemResponses = client.prepareMultiGet().add(index, type, id1, id2).get();
            List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
            for (MultiGetItemResponse itemResponse : multiGetItemResponses) {
                GetResponse response = itemResponse.getResponse();
                if (response.isExists()) {
                    lists.add(response.getSource());
                }
            }
            return lists;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    /**
     *@Title: addDocuments  批量新增记录  注意 下面有个map.get(id) 也就是物理表的id
     *@Description: TODO
     *@param @param list
     *@param @param index
     *@param @param type
     *@return void
     *@throws
     */
    public static void addDocuments(List<Map<Object, Object>> list, String index, String type) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));

            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for (Map<Object, Object> map : list) {
                //遍历map所有field,构造插入对象
                XContentBuilder xb = XContentFactory.jsonBuilder().startObject();
                for (Object key : map.keySet()) {
                    xb.field((String) key, map.get(key));
                }
                xb.endObject();
                //id尽量为物理表的主键
                bulkRequest.add(client.prepareIndex(index, type, StringUtils.trim(map.get("id").toString())).setSource(xb));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                System.err.println("");
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     *@Title: queryDocuments
     *@Description: TODO
     *@param @param index  相当于库
     *@param @param type   想当于表
     *@param @param from 记录从哪开始
     *@param @param size 数量
     *@param @param rangeLists  范围 参数比如价格   key为   field,from,to
     *@param @param queryMaps  精确查询参数
     *@param @param sortMaps  排序参数  key为   field  value传大写的 ASC , DESC
     * *@param @param fields  要高亮的字段
     *@param @return
     *@return List<Map<String,Object>>
     *@throws
     */
    public static List<Map<String, Object>> queryDocuments(String index, String type, int from, int size, List<Map<Object, Object>> rangeLists, Map<Object, Object> queryMaps, Map<Object, Object> sortMaps, List<String> fields, Map<Object, Object> fullTextQueryMaps) {
        try {
            Client client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));

            List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
            /** 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索 **/
            //构造全文或关系的查询
            BoolQueryBuilder bb = QueryBuilders.boolQuery();
            if (fullTextQueryMaps != null) {
                for (Object key : fullTextQueryMaps.keySet()) {
                    bb = bb.should(QueryBuilders.matchQuery((String) key, fullTextQueryMaps.get(key)));
                }
            }

            //构造精确的并且查询
            BoolQueryBuilder bb1 = QueryBuilders.boolQuery();
            if (queryMaps != null) {
                bb1 = bb1.must(bb);
                for (Object key : queryMaps.keySet()) {
                    bb1 = bb1.must(QueryBuilders.termQuery((String) key, queryMaps.get(key)));
                }
            }
            /** 上面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索 **/
            //match全文检索，但是并且的关系， 或者的关系要用
            /*MatchQueryBuilder tq = null;
            if (queryMaps != null) {
                for (Object key : queryMaps.keySet()) {
                    tq = QueryBuilders.matchQuery((String) key, queryMaps.get(key));
                }
            }*/

            //term是代表完全匹配，即不进行分词器分析，文档中必须包含整个搜索的词汇
            /*  TermQueryBuilder tq = null;
                if (queryMaps != null) {
                    for (Object key : queryMaps.keySet()) {
                        tq = QueryBuilders.termQuery((String) key, queryMaps.get(key));
                    }
                }*/

            //构造范围查询参数
            QueryBuilder qb = null;
            if (rangeLists != null && rangeLists.size() > 0) {

                for (Map<Object, Object> map : rangeLists) {
                    if (map != null && (!map.isEmpty())) {
                        qb = QueryBuilders.rangeQuery(StringUtils.trim(map.get("field").toString())).from(StringUtils.trim(map.get("from").toString())).to(StringUtils.trim(map.get("to").toString()));
                    }
                }
            }
            //构造排序参数
            SortBuilder sortBuilder = null;
            if (sortMaps != null) {
                for (Object key : sortMaps.keySet()) {
                    sortBuilder = SortBuilders.fieldSort((String) key).order(StringUtils.trim(sortMaps.get(key).toString()).equals("ASC") ? SortOrder.ASC : SortOrder.DESC);
                }
            }

            //构造查询
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index).setTypes(type).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(bb1) // Query
                    .setPostFilter(qb) // Filter
                    .setFrom(from).setSize(size).addSort(sortBuilder).setExplain(true);
            //构造高亮字段
            if (fields != null && fields.size() > 0) {
                for (String field : fields) {
                    searchRequestBuilder.addHighlightedField(field);
                }
                searchRequestBuilder.setHighlighterEncoder("UTF-8").setHighlighterPreTags("<span style=\"color:red\">").setHighlighterPostTags("</span>");
            }

            //查询
            SearchResponse response = searchRequestBuilder.execute().actionGet();
            //取值
            SearchHits hits = response.getHits();
            System.out.println(hits.totalHits());
            for (SearchHit hit : hits) {

                Map<String, HighlightField> result = hit.highlightFields();

                //用高亮字段替换搜索字段
                for (String field : fields) {
                    HighlightField titleField = result.get(field);
                    if (titleField == null) {
                        continue;
                    }
                    Text[] titleTexts = titleField.fragments();
                    String value = "";
                    for (Text text : titleTexts) {

                        value += text;
                    }
                    hit.getSource().put(field, value);
                }
                lists.add(hit.getSource());
                //System.out.println(hit.getSource());
                //System.out.println(hit.getHighlightFields());
                //System.out.println(hit.getSourceAsString());//json格式

            }
            return lists;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

}

