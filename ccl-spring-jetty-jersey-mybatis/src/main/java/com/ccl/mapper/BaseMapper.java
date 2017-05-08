package com.ccl.mapper;

import com.ccl.model.BaseModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T> {

    public void add(T t);

    public void update(T t);

    public void updateBySelective(T t);

    public void delete(Object id);

    public int queryByCount(BaseModel model);

    public List<T> queryByList(BaseModel model);

    public T queryById(Object id);

    public List<T> queryListByWhere(@Param("where") String where);

    public int queryCountByWhere(@Param("where") String where);

    public void updateByWhere(@Param("where") String where);

    public List<T> queryAllByList(BaseModel model);
}
