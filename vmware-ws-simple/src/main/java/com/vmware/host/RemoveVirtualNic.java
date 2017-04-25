/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.host;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 * RemoveVirtualNic
 *
 * This sample removes a Virtual Nic from a PortGroup on a vSwitch
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * portgroupname    [required] : Name of port group to remove Virtual Nic from
 * hostname         [required] : Name of host
 * datacentername   [optional] : Name of datacenter
 *
 * <b>Command Line:</b>
 * Remove a VirtualNic from a PortGroup
 * run.bat com.vmware.host.RemoveVirtualNic --url [webserviceurl]
 * --username [username] --password  [password] --datacentername [mydatacenter]
 * --portgroupname [myportgroup] --hostname [hostname]
 *
 * Remove a VirtualNic from a PortGroup without hostname
 * run.bat com.vmware.host.RemoveVirtualNic --url [webserviceurl]
 * --username [username] --password  [password] --datacentername [mydatacenter]
 * --portgroupname [myportgroup]
 *
 * Remove a VirtualNic from a PortGroup without datacentername
 * run.bat com.vmware.host.RemoveVirtualNic --url [webserviceurl]
 * --username [username] --password  [password]
 * --portgroupname [myportgroup] --hostname [name of the host]
 * </pre>
 */
@Sample(
        name = "remove-virtual-nic",
        description = "removes a Virtual Nic from a PortGroup on a vSwitch"
)
public class RemoveVirtualNic extends ConnectedVimServiceBase {
    ManagedObjectReference propCollectorRef = null;
    ManagedObjectReference rootFolder;

    String datacenter;
    String host;
    String portgroupname;

    @Option(name = "datacentername", required = false, description = "Name of datacenter")
    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    @Option(name = "hostname", description = "name of host")
    public void setHost(String host) {
        this.host = host;
    }

    @Option(name = "portgroupname", description = "Name of port group to remove Virtual Nic from")
    public void setPortgroupname(String portgroupname) {
        this.portgroupname = portgroupname;
    }

    void init() {
        propCollectorRef = serviceContent.getPropertyCollector();
        rootFolder = serviceContent.getRootFolder();
    }

    /**
     * @return TraversalSpec specification to get to the Datacenter managed
     *         object.
     */
    TraversalSpec getDatacenterTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        //TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);
        traversalSpec.getSelectSet().add(sSpec);
        return traversalSpec;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs,
                        propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts =
                    vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }

    /**
     * @param datacenterName The name of the Datacenter
     * @return ManagedObjectReference to the Datacenter
     */
    ManagedObjectReference getDatacenterByName(
            String datacenterName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;

        TraversalSpec tSpec = getDatacenterTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("Datacenter");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolder);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().add(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);

        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                ManagedObjectReference mr = oc.getObj();
                String dcnm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    //Since there is only one property PropertySpec pathset
                    //this array contains only one value
                    for (DynamicProperty dp : dps) {
                        dcnm = (String) dp.getVal();
                    }
                }
                //This is done outside of the previous for loop to break
                //out of the loop as soon as the required datacenter is found.
                if (dcnm != null && dcnm.equals(datacenterName)) {
                    retVal = mr;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Retrieve contents for a single object based on the property collector
     * registered with the service.
     *
     * @param mobj       Managed Object Reference to get contents for
     * @param properties names of properties of object to retrieve
     * @return retrieved object contents
     */
    ObjectContent[] getObjectProperties(
            ManagedObjectReference mobj, String[] properties) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        if (mobj == null) {
            return null;
        }

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(new PropertySpec());
        if ((properties == null || properties.length == 0)) {
            spec.getPropSet().get(0).setAll(Boolean.TRUE);
        } else {
            spec.getPropSet().get(0).setAll(Boolean.FALSE);
        }
        spec.getPropSet().get(0).setType(mobj.getType());
        spec.getPropSet().get(0).getPathSet().addAll(Arrays.asList(properties));
        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(mobj);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent> listobcont = retrievePropertiesAllObjects(listpfs);
        return listobcont.toArray(new ObjectContent[listobcont.size()]);
    }

    /**
     * Determines of a method 'methodName' exists for the Object 'obj'.
     *
     * @param obj            The Object to check
     * @param methodName     The method name
     * @param parameterTypes Array of Class objects for the parameter types
     * @return true if the method exists, false otherwise
     */
    @SuppressWarnings("rawtypes")
    boolean methodExists(Object obj, String methodName,
                         Class[] parameterTypes) throws NoSuchMethodException {
        boolean exists = false;
        Method method = obj.getClass().getMethod(methodName, parameterTypes);
        if (method != null) {
            exists = true;
        }
        return exists;
    }

    /**
     * Retrieve a single object.
     *
     * @param mor          Managed Object Reference to get contents for
     * @param propertyName of the object to retrieve
     * @return retrieved object
     */
    Object getDynamicProperty(ManagedObjectReference mor,
                              String propertyName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ObjectContent[] objContent =
                getObjectProperties(mor, new String[]{propertyName});

        Object propertyValue = null;
        if (objContent != null) {
            List<DynamicProperty> listdp = objContent[0].getPropSet();
            if (listdp != null) {
                /*
                * Check the dynamic propery for ArrayOfXXX object
                */
                Object dynamicPropertyVal = listdp.get(0).getVal();
                String dynamicPropertyName =
                        dynamicPropertyVal.getClass().getName();
                if (dynamicPropertyName.indexOf("ArrayOf") != -1) {
                    String methodName =
                            dynamicPropertyName.substring(
                                    dynamicPropertyName.indexOf("ArrayOf")
                                            + "ArrayOf".length(),
                                    dynamicPropertyName.length());
                    /*
                    * If object is ArrayOfYYY object, then get the YYY[] by
                    * invoking getYYY() on the object.
                    * For Ex:
                    * ArrayOfManagedObjectReference.getManagedObjectReference()
                    * returns ManagedObjectReference[] array.
                    */
                    if (methodExists(dynamicPropertyVal, "get" + methodName, null)) {
                        methodName = "get" + methodName;
                    } else {
                        /*
                        * Construct methodName for ArrayOf primitive types
                        * Ex: For ArrayOfInt, methodName is get_int
                        */
                        methodName = "get_" + methodName.toLowerCase();
                    }
                    Method getMorMethod =
                            dynamicPropertyVal.getClass().getDeclaredMethod(
                                    methodName, (Class[]) null);
                    propertyValue =
                            getMorMethod.invoke(dynamicPropertyVal, (Object[]) null);
                } else if (dynamicPropertyVal.getClass().isArray()) {
                    /*
                    * Handle the case of an unwrapped array being deserialized.
                    */
                    propertyValue = dynamicPropertyVal;
                } else {
                    propertyValue = dynamicPropertyVal;
                }
            }
        }
        return propertyValue;
    }

    /**
     * Get a MORef from the property returned.
     *
     * @param objMor   Object to get a reference property from
     * @param propName name of the property that is the MORef
     * @return the ManagedObjectReference for that property.
     */
    ManagedObjectReference getMorFromPropertyName(
            ManagedObjectReference objMor, String propName) throws RuntimeFaultFaultMsg, InvocationTargetException, InvalidPropertyFaultMsg, NoSuchMethodException, IllegalAccessException {
        Object props = getDynamicProperty(objMor, propName);
        ManagedObjectReference propmor = null;
        if (!props.getClass().isArray()) {
            propmor = (ManagedObjectReference) props;
        }
        return propmor;
    }

    String getEntityName(ManagedObjectReference obj,
                         String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String retVal = null;

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType(entityType);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(obj);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        retVal = (String) dp.getVal();
                        return retVal;
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * @return An array of SelectionSpec covering all the entities that provide
     *         performance statistics. The entities that provide performance
     *         statistics are VM, Host, Resource pool, Cluster Compute Resource
     *         and Datastore.
     */
    SelectionSpec[] buildFullTraversal() {

        // Terminal traversal specs

        // RP -> VM
        TraversalSpec rpToVm = new TraversalSpec();
        rpToVm.setName("rpToVm");
        rpToVm.setType("ResourcePool");
        rpToVm.setPath("vm");
        rpToVm.setSkip(Boolean.FALSE);

        // vApp -> VM
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        // HostSystem -> VM
        TraversalSpec hToVm = new TraversalSpec();
        hToVm.setType("HostSystem");
        hToVm.setPath("vm");
        hToVm.setName("HToVm");
        hToVm.setSkip(Boolean.FALSE);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        // For RP -> RP recursion
        SelectionSpec rpToRpSpec = new SelectionSpec();
        rpToRpSpec.setName("rpToRp");

        // Recurse through all ResourcePools
        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        rpToRp.setName("rpToRp");
        SelectionSpec[] sspecs = new SelectionSpec[]{rpToRpSpec};
        rpToRp.getSelectSet().addAll(Arrays.asList(sspecs));

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        SelectionSpec[] sspecarrayrptorprtptovm = new SelectionSpec[]{rpToRp};
        crToRp.getSelectSet().addAll(Arrays.asList(sspecarrayrptorprtptovm));

        TraversalSpec crToH = new TraversalSpec();
        crToH.setSkip(Boolean.FALSE);
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setName("crToH");
        crToH.getSelectSet().add(hToVm);

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setSkip(Boolean.FALSE);
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setName("dcToHf");
        dcToHf.getSelectSet().add(sspecvfolders);

        TraversalSpec vAppToRp = new TraversalSpec();
        vAppToRp.setName("vAppToRp");
        vAppToRp.setType("VirtualApp");
        vAppToRp.setPath("resourcePool");
        SelectionSpec[] vAppToVMSS = new SelectionSpec[]{rpToRpSpec};
        vAppToRp.getSelectSet().addAll(Arrays.asList(vAppToVMSS));

        TraversalSpec dcToVmf = new TraversalSpec();
        dcToVmf.setType("Datacenter");
        dcToVmf.setSkip(Boolean.FALSE);
        dcToVmf.setPath("vmFolder");
        dcToVmf.setName("dcToVmf");
        dcToVmf.getSelectSet().add(sspecvfolders);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(crToRp);
        sspecarrvf.add(crToH);
        sspecarrvf.add(dcToVmf);
        sspecarrvf.add(dcToHf);
        sspecarrvf.add(vAppToRp);
        sspecarrvf.add(vAppToVM);
        sspecarrvf.add(dcToDs);
        sspecarrvf.add(rpToVm);
        sspecarrvf.add(sspecvfolders);
        visitFolders.getSelectSet().addAll(sspecarrvf);
        return new SelectionSpec[]{visitFolders};
    }

    /**
     * Getting the MOREF of the entity.
     */
    ManagedObjectReference getEntityByName(String entityName,
                                           String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(entityType);
        propertySpec.getPathSet().add("name");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootFolder);
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().addAll(Arrays.asList(buildFullTraversal()));

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                if (getEntityName(oc.getObj(), entityType).equals(entityName)) {
                    retVal = oc.getObj();
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Getting the MOREF of the entity.
     */
    List<ManagedObjectReference> getMorList(
            ManagedObjectReference mor, String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<ManagedObjectReference> retVal =
                new ArrayList<ManagedObjectReference>();

        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.setType(entityType);
        propertySpec.getPathSet().add("name");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        if (mor == null) {
            objectSpec.setObj(rootFolder);
        } else {
            objectSpec.setObj(mor);
        }
        objectSpec.setSkip(Boolean.TRUE);
        objectSpec.getSelectSet().addAll(Arrays.asList(buildFullTraversal()));

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                retVal.add(oc.getObj());
            }
        }
        return retVal;
    }

    /**
     * Getting the MOREF of the entity.
     */
    ManagedObjectReference getFirstEntityByMOR(
            ManagedObjectReference mor, String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<ManagedObjectReference> listmors = getMorList(mor, entityType);
        ManagedObjectReference retval = null;
        if (listmors.size() > 0) {
            return listmors.get(0);
        }
        return retval;
    }

    /**
     * This method returns a MoRef to the HostSystem with the supplied name under
     * the supplied Folder. If hostname is null, it returns the first HostSystem
     * found under the supplied Folder
     *
     * @param hostFolderMor MoRef to the Folder to look in
     * @param hostname      Name of the HostSystem you are looking for
     * @return MoRef to the HostSystem or null if not found
     * @throws Exception
     */
    ManagedObjectReference getHost(
            ManagedObjectReference hostFolderMor, String hostname) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        ManagedObjectReference hostmor = null;
        if (hostname != null) {
            hostmor = getEntityByName(hostname, "HostSystem");
        } else {
            hostmor = getFirstEntityByMOR(hostFolderMor, "HostSystem");
        }
        return hostmor;
    }

    Object getHostConfigManagerrByHostMor(
            ManagedObjectReference hostmor) throws RuntimeFaultFaultMsg, InvocationTargetException, InvalidPropertyFaultMsg, NoSuchMethodException, IllegalAccessException {
        return getDynamicProperty(hostmor, "configManager");
    }

    @SuppressWarnings("unchecked")
    void removeVirtualNic() {
        ManagedObjectReference dcmor;
        ManagedObjectReference hostfoldermor;
        ManagedObjectReference hostmor = null;

        try {
            if (((datacenter != null) && (host != null))
                    || ((datacenter != null) && (host == null))) {
                dcmor = getDatacenterByName(datacenter);
                if (dcmor == null) {
                    System.out.println("Datacenter not found");
                    return;
                }
                hostfoldermor = getMorFromPropertyName(dcmor, "hostFolder");
                hostmor = getHost(hostfoldermor, host);
            } else if ((datacenter == null) && (host != null)) {
                hostmor = getHost(null, host);
            }

            if (hostmor != null) {
                Object cmobj = getHostConfigManagerrByHostMor(hostmor);
                HostConfigManager configMgr = (HostConfigManager) cmobj;
                ManagedObjectReference nwSystem = configMgr.getNetworkSystem();

                List<HostVirtualNic> hvncArr =
                        (List<HostVirtualNic>) getDynamicProperty(nwSystem,
                                "networkInfo.vnic");
                boolean foundOne = false;
                for (HostVirtualNic nic : hvncArr) {
                    String portGroup = nic.getPortgroup();
                    if (portGroup.equals(portgroupname)) {
                        vimPort.removeVirtualNic(nwSystem, nic.getDevice());
                        foundOne = true;
                    }
                }
                if (foundOne) {
                    System.out
                            .println("Successfully removed virtual nic from portgroup : "
                                    + portgroupname);
                } else {
                    System.out.println("No virtual nic found on portgroup : "
                            + portgroupname);
                }
            } else {
                System.out.println("Host not found");
            }
        } catch (HostConfigFaultFaultMsg ex) {
            System.out.println("Failed : Configuration falilures. ");
        } catch (NotFoundFaultMsg ex) {
            System.out.println("Failed : " + ex);
        } catch (RuntimeFaultFaultMsg ex) {
            System.out.println("Failed : " + ex);
        } catch (Exception e) {
            System.out.println("Failed : " + e);
        }

    }

    @Action
    public void run() {
        init();
        removeVirtualNic();
    }

}
