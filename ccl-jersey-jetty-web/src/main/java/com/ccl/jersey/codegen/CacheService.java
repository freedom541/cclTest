package com.ccl.jersey.codegen;

import java.util.List;
import java.util.Map;

/**
 * 缓存服务
 * 
 * @author ccl
 * 
 */
public interface CacheService {

	/**
	 * 设置值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 * @param seconds
	 *            到期时间秒数
	 */
	public void set(String key, Object value, int seconds);

	/**
	 * 设置值
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void set(String key, Object value);

	/**
	 * 设置到期值
	 * 
	 * @param key
	 *            键
	 * @param seconds
	 *            到期时间秒数
	 */
	public void expire(String key, int seconds);

	/**
	 * 获取值
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public Object get(String key);

	/**
	 * 批量获取值
	 * 
	 * @param keys
	 * @return
	 */
	public Map<String, Object> getBulk(List<String> keys);

	/**
	 * 检测值是否存在
	 * 
	 * @param key
	 *            键
	 * @return
	 */
	public boolean exists(String key);

	/**
	 * 删除值
	 * 
	 * @param key
	 *            键
	 */
	public void delete(String key);
}
