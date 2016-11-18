package com.ccl.elasticsearch.one;

/**
 * Created by ccl on 16/11/15.
 */
public class GoodsFilter2ES {
    private String regionId; // 园区UUID

    private String queryStr; // 条件

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getQueryStr() {
        return queryStr;
    }

    public void setQueryStr(String queryStr) {
        this.queryStr = queryStr;
    }
}
