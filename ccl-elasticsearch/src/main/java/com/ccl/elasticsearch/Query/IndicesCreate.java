package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class IndicesCreate {
    public static void main(String[] args) {
        Client client = null;
        try {
            client = EsUtil.getTransportClient();
            // delete
            client.admin().indices().prepareDelete("product").execute()
                    .actionGet();
            // create
            client.admin().indices().prepareCreate("product").execute()
                    .actionGet();
            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject().startObject("properties").startObject("id")
                    .field("type", "string").endObject().startObject("id2")
                    .field("type", "string").field("index", "not_analyzed")
                    .endObject().startObject("name").field("type", "string")
                    .endObject().startObject("age").field("type", "integer")
                    .endObject().startObject("salary").field("type", "double")
                    .endObject().endObject();
            PutMappingRequest paramPutMappingRequest = Requests
                    .putMappingRequest("product").type("user").source(mapping);
            client.admin().indices().putMapping(paramPutMappingRequest)
                    .actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EsUtil.close(client);
        }
    }
}