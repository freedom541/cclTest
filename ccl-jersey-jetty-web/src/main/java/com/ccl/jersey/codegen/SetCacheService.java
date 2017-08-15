package com.ccl.jersey.codegen;

import java.util.Set;

/**
 * 缓存队列服务
 * 
 * @author ccl
 *
 */
public interface SetCacheService extends CacheService {

	/**
	 * 放值
	 * 
	 * @param key
	 * @param value
	 */
	public void add(String key, Object value);

	/**
	 * 取值并刪除
	 * 
	 * @param key
	 * @return
	 */
	public Object pop(String key);

	/**
	 * 删除值
	 * 
	 * @param key
	 * @param value
	 */
	public void remove(String key, Object value);

	/**
	 * 队列长度
	 * 
	 * @param key
	 * @return
	 */
	public long size(String key);

	/**
	 * 是否存在该值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean isMember(String key, Object value);

    /**
     * 列表
     * @param key
     * @return
     */
    public Set<Object> members(String key);

}
