package com.ccl.jersey.codegen;

import org.joda.time.DateTime;

/**
 * Created by Dean on 2015/7/3.
 * <p/>
 * 包括统计字段的实体
 */
public interface StatisticsEntity {

    /**
     * 设置创建时间
     *
     * @param createTime
     */
    void setCreateTime(DateTime createTime);

    /**
     * 获取创建时间
     *
     * @return
     */
    DateTime getCreateTime();

    /**
     * 设置更新时间
     *
     * @param updateTime
     */
    void setUpdateTime(DateTime updateTime);

    /**
     * 获取更新时间
     *
     * @return
     */
    DateTime getUpdateTime();
}
