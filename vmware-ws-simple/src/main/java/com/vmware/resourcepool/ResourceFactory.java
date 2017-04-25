package com.vmware.resourcepool;

/**
 * Created by ccl on 17/3/13.
 * 资源工厂
 */
public interface ResourceFactory<T> {
    /**
     * 创建资源
     */
    public T createResource();

    /**
     * 验证资源是否有效
     */
    public boolean validateResource(T o);

}
