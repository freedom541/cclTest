package com.vmware.test.netCode;

/**
 * Created by ccl on 17/2/17.
 */
import com.vmware.vim25.*;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Di
 * @ 获取数据中心对象引用及name属性
 * @date 创建时间：2016年12月8日 下午4:01:33
 * @version 1.0
 */
public class GetDataCenter {

    /**
     * @ 获取遍历数据中心的TraversalSpec
     * @explain:清单遍历一定要结合清单遍历结构图。
     * @return TraversalSpec:清单遍历对象，明确属性收集的对象类型，同事提供对象遍历的路径。
     * */
    public static TraversalSpec getDatacenterTraversalSpec()
    {
        //SelectionSpec是TraversalSpec的一个引用。
        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        TraversalSpec traversalSpec = new TraversalSpec();
        //给traversalSpec设置名称
        traversalSpec.setName("VisitFolders");
        //从rootFolder开始遍历，rootFolder类型是Folder
        traversalSpec.setType("Folder");
        //rootFolder拥有childEntity属性，清单结构图中指向的便是Datacenter
        traversalSpec.setPath("childEntity");
        //false表示不对其本身进行收集，只对其下对象进行收集
        traversalSpec.setSkip(false);
        //将sSpec添加到SelectionSpec集合中
        traversalSpec.getSelectSet().add(sSpec);
        return traversalSpec;
    }

    /**
     * @ 获取出所有的数据中心
     * @return retVal:数据中心对象引用list。
     * */
    public static List<ManagedObjectReference> getAllDatacenter()
    {
        List<ManagedObjectReference> retVal = new ArrayList<ManagedObjectReference>();
        //获取根目录对象引用
        ManagedObjectReference rootFolder = MoniterWsInterface.serviceContent.getRootFolder();
        try
        {
            TraversalSpec tSpec = getDatacenterTraversalSpec();

            /**
             * ObjectSpec：定义对象详述，明确清单导航起始点。
             * obj:定义遍历起始对象为根目录rootFolder
             * true:表示只收集Datacenter的数据，不收集containerView的数据。
             * */
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(rootFolder);
            objectSpec.setSkip(Boolean.TRUE);

            /** 添加 tSpec到 ObjectSpec.selectSet队列中 */
            objectSpec.getSelectSet().add(tSpec);

            /**
             * PropertySpec：定义一个属性收集器详述，明确收集的具体对象(Datacenter)和属性(Datacenter中的name，可以为多个)
             * Type:具体的对象类型为Datacenter
             * pathset:明确对象Datacenter中的属性，可以为多个。
             * */
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.getPathSet().add("name");
            propertySpec.setType("Datacenter");

            /**
             * PropertyFilterSpec:定义一个属性过滤器详述，添加对象详述和属性收集器详述到过率中
             * */
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            /** 添加属性过滤器详述到属性过滤器集合中 */
            List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
            listfps.add(propertyFilterSpec);

            /** 调用方法获取ObjectContent对象集合 */
            List<ObjectContent> listobcont = MoniterWsInterface.retrievePropertiesAllObjects(listfps);

            if (listobcont != null)
            {
                for (ObjectContent oc : listobcont)
                {
                    //根据object对象获得MOR对象
                    ManagedObjectReference mr = oc.getObj();

                    String dcnm = null;
                    //获取属性集合(此处只有一个name属性)
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps != null)
                    {
                        for (DynamicProperty dp : dps)
                        {
                            //获取到具体的数据中心(Datacenter)的名称
                            dcnm = (String) dp.getVal();
                            //System.out.println("数据中心名称"+dcnm);
                            retVal.add(mr);
                        }
                    }
                }
            }
        }
        catch (SOAPFaultException sfe)
        {
            MoniterWsInterface.printSoapFaultException(sfe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * @ 获取出所有的数据中心，和上面方法一样，只是最后返回值取得是Datacenter的属性name的值，而非Datacenter的对象引用。
     * @return retVal:数据中心名称list。
     * */
    public static List<String> getDatacenterName()
    {
        List<String> retVal = new ArrayList<String>();
        ManagedObjectReference rootFolder = MoniterWsInterface.serviceContent.getRootFolder();
        try
        {
            TraversalSpec tSpec = getDatacenterTraversalSpec();

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(rootFolder);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(tSpec);

            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.getPathSet().add("name");
            propertySpec.setType("Datacenter");

            //添加对象和属性声明到 PropertyFilterSpec。
            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
            listfps.add(propertyFilterSpec);

            List<ObjectContent> listobcont = MoniterWsInterface.retrievePropertiesAllObjects(listfps);

            if (listobcont != null)
            {
                for (ObjectContent oc : listobcont)
                {
                    //根据object对象获得MOR对象
                    ManagedObjectReference mr = oc.getObj();

                    String dcnm = null;
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps != null)
                    {
                        for (DynamicProperty dp : dps)
                        {
                            dcnm = (String) dp.getVal();
                            retVal.add(dcnm);
                        }
                    }
                }
            }
        }
        catch (SOAPFaultException sfe)
        {
            MoniterWsInterface.printSoapFaultException(sfe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * @ main测试方法
     * */
    public static void main(String[] args) throws Exception {
        MoniterWsInterface moniterWsInterface = new MoniterWsInterface();
        moniterWsInterface.connect();

       /* List<ManagedObjectReference> allDatacenter = getAllDatacenter();
        for (ManagedObjectReference dataCenter : allDatacenter) {
            System.out.println(dataCenter.getType());
        }*/
        List<String> datacenterName = getDatacenterName();
        for (String name : datacenterName) {
            System.out.println(name);
        }
        moniterWsInterface.disconnect();
    }
}