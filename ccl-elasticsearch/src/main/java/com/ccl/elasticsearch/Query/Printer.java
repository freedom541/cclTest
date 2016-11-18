package com.ccl.elasticsearch.Query;

/**
 * Created by ccl on 16/11/16.
 */
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Map;

public class Printer {
    public static void print(SearchHits hits) {
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSource();
            System.out.println(map);
        }
    }
}

