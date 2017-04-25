package com.vmware.resourcepool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ccl on 17/3/13.
 * 资源池
 */
public class ResourcePool<T> {
    private ResourceFactory<T> factory;//资源池工厂
    private int maxResource;//资源池最大限度
    private int currentResource;//当前资源数量
    private boolean quit;//资源池是否被销毁
    private Set<T> takeResource;//所取得资源
    private List<T> waitResource;//所有等待的资源


    /**
     * 资源池构造
     */
    public ResourcePool(ResourceFactory factory, int maxResource){
        this.factory = factory;
        this.maxResource = maxResource;
        this.currentResource = 0;
        this.takeResource = new HashSet<T>();
        this.waitResource = new ArrayList<T>();
    }

    /**
     * 获取资源
     */
    public synchronized T getResource() throws Exception {
        while (!this.quit){
            //首先查找等待资源
            if (!this.waitResource.isEmpty()){
                T o = this.waitResource.get(0);
                //如果资源无效,创建一个替换者
                if (this.factory.validateResource(o)){
                    o = factory.createResource();
                }
                this.takeResource.add(o);
                return o;
            }

            //如果是为超出最大资源池限度,创建新的资源
            if (this.currentResource < this.maxResource){
                T o = this.factory.createResource();
                this.takeResource.add(o);
                this.currentResource++;
                return o;
            }

            //如果等待资源,并且已经达到资源池的最大限度,一直处于等待状态
            try {
                this.wait();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //资源池被销毁
        return null;
    }


    /**
     * 返回资源到资源池
     */
    public synchronized void returnResource(T o){
        if (!this.takeResource.remove(o)){
            throw new IllegalStateException("对象不在资源池中,无法返回");
        }
        this.waitResource.add(o);
        //唤醒一个等待的线程
        this.notify();
    }


    /**
     * 销毁资源池
     */
    public synchronized void destory(){
        this.quit = true;
        this.notifyAll();
    }


    public ResourceFactory getFactory() {
        return factory;
    }

    public void setFactory(ResourceFactory factory) {
        this.factory = factory;
    }

    public int getMaxResource() {
        return maxResource;
    }

    public void setMaxResource(int maxResource) {
        this.maxResource = maxResource;
    }

    public int getCurrentResource() {
        return currentResource;
    }

    public void setCurrentResource(int currentResource) {
        this.currentResource = currentResource;
    }

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public Set<T> getTakeResource() {
        return takeResource;
    }

    public void setTakeResource(Set<T> takeResource) {
        this.takeResource = takeResource;
    }

    public List<T> getWaitResource() {
        return waitResource;
    }

    public void setWaitResource(List<T> waitResource) {
        this.waitResource = waitResource;
    }
}
