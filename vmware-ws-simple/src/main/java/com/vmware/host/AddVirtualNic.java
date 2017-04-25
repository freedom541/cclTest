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
 * AddVirtualNic
 *
 * This sample is used to add a Virtual Nic to a PortGroup
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * portgroupname    [required] : Name of the port group
 * ipaddress        [optional] : ipaddress for the nic, if not set DHCP
 *                               will be in affect for the nic
 * hostname         [optional] : Name of the host
 * datacentername   [optional] : Name of the datacenter
 *
 * <b>Command Line:</b>
 * Add VirtualNic to a PortGroup on a Virtual Switch
 * run.bat com.vmware.host.AddVirtualNic --url [webserviceurl]
 * --username [username] --password  [password]
 * --hostname [hostname]  --datacentername [mydatacenter]
 * --portgroupname [myportgroup] --ipaddress [AAA.AAA.AAA.AAA]
 *
 * Add VirtualNic to a PortGroup on a Virtual Switch without hostname
 * run.bat com.vmware.host.AddVirtualNic --url [webserviceurl]
 * --username [username] --password  [password]
 * --datacentername [mydatacenter]
 * --portgroupname [myportgroup] --ipaddress [AAA.AAA.AAA.AAA]
 *
 * Add VirtualNic to a PortGroup on a Virtual Switch without datacentername
 * run.bat com.vmware.host.AddVirtualNic --url [webserviceurl]
 * --username [username] --password  [password]
 * --portgroupname [myportgroup] --ipaddress [AAA.AAA.AAA.AAA]
 * </pre>
 */
@Sample(name = "add-virtual-nic", description = "This sample is used to add a Virtual Nic to a PortGroup")
public class AddVirtualNic extends ConnectedVimServiceBase {
    private ManagedObjectReference rootFolder;
    private ManagedObjectReference propCollectorRef;

    String datacentername;
    String hostname;
    String portgroupname;
    String ipaddress;

    @Option(name = "portgroupname", required = true, description = "Name of the port group")
    public void setPortgroupname(String portgroupname) {
        this.portgroupname = portgroupname;
    }

    @Option(
            name = "ipaddress",
            required = false,
            description = "ipaddress for the nic, if not set DHCP will be in affect for the nic"
    )
    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    @Option(name = "hostname", required = false, description = "Name of the host")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Option(name = "datacentername", required = false, description = "Name of the datacenter")
    public void setDatacentername(String datacentername) {
        this.datacentername = datacentername;
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
        List<PropertyFilterSpec> pfspec = new ArrayList<PropertyFilterSpec>(1);
        pfspec.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(pfspec);
        return listobjcont.toArray(new ObjectContent[listobjcont.size()]);
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
                              String propertyName) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
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
                    if (methodExists(dynamicPropertyVal, "get" + methodName, null)) {
                        methodName = "get" + methodName;
                    } else {
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
            ManagedObjectReference objMor, String propName) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        Object props = getDynamicProperty(objMor, propName);
        ManagedObjectReference propmor = null;
        if (!props.getClass().isArray()) {
            propmor = (ManagedObjectReference) props;
        }
        return propmor;
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
     * Getting the MOREF of the entity.
     */
    ManagedObjectReference getEntityByName(
            ManagedObjectReference mor, String entityName, String entityType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;

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
     * @param hostName      Name of the HostSystem you are looking for
     * @return MoRef to the HostSystem or null if not found
     * @throws Exception
     */

    ManagedObjectReference getHost(
            ManagedObjectReference hostFolderMor, String hostName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference hostmor = null;
        if (hostName != null) {
            hostmor = getEntityByName(hostFolderMor, hostName, "HostSystem");
        } else {
            hostmor = getFirstEntityByMOR(hostFolderMor, "HostSystem");
        }

        return hostmor;
    }

    /**
     * @return TraversalSpec specification to get to the HostSystem managed
     *         object.
     */
    TraversalSpec getHostSystemTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the Host system.
        // Build the traversal specs bottoms up
        SelectionSpec ss = new SelectionSpec();
        ss.setName("VisitFolders");

        // Traversal to get to the host from ComputeResource
        TraversalSpec computeResourceToHostSystem = new TraversalSpec();
        computeResourceToHostSystem.setName("computeResourceToHostSystem");
        computeResourceToHostSystem.setType("ComputeResource");
        computeResourceToHostSystem.setPath("host");
        computeResourceToHostSystem.setSkip(false);
        computeResourceToHostSystem.getSelectSet().add(ss);

        // Traversal to get to the ComputeResource from hostFolder
        TraversalSpec hostFolderToComputeResource = new TraversalSpec();
        hostFolderToComputeResource.setName("hostFolderToComputeResource");
        hostFolderToComputeResource.setType("Folder");
        hostFolderToComputeResource.setPath("childEntity");
        hostFolderToComputeResource.setSkip(false);
        hostFolderToComputeResource.getSelectSet().add(ss);

        // Traversal to get to the hostFolder from DataCenter
        TraversalSpec dataCenterToHostFolder = new TraversalSpec();
        dataCenterToHostFolder.setName("DataCenterToHostFolder");
        dataCenterToHostFolder.setType("Datacenter");
        dataCenterToHostFolder.setPath("hostFolder");
        dataCenterToHostFolder.setSkip(false);
        dataCenterToHostFolder.getSelectSet().add(ss);

        //TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);

        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(ss);
        sSpecArr.add(dataCenterToHostFolder);
        sSpecArr.add(hostFolderToComputeResource);
        sSpecArr.add(computeResourceToHostSystem);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
    }


    /**
     * Retrieves the MOREF of the host.
     *
     * @param hostName :
     * @return
     */
    ManagedObjectReference getHostByHostName(String hostName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;

        TraversalSpec tSpec = getHostSystemTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("HostSystem");

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
                String hostnm = null;
                List<DynamicProperty> listDynamicProps = oc.getPropSet();
                DynamicProperty[] dps =
                        listDynamicProps
                                .toArray(new DynamicProperty[listDynamicProps.size()]);
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        hostnm = (String) dp.getVal();
                    }
                }
                if (hostnm != null && hostnm.equals(hostName)) {
                    retVal = mr;
                    break;
                }
            }
        } else {
            System.out.println("The Object Content is Null");
        }
        return retVal;
    }

    HostVirtualNicSpec createVirtualNicSpecification() {
        HostIpConfig hipconfig = new HostIpConfig();
        if (ipaddress != null && !ipaddress.isEmpty()) {
            hipconfig.setDhcp(Boolean.FALSE);
            hipconfig.setIpAddress(ipaddress);
            hipconfig.setSubnetMask("255.255.255.0");
        } else {
            hipconfig.setDhcp(Boolean.TRUE);
        }
        HostVirtualNicSpec hvnicspec = new HostVirtualNicSpec();
        hvnicspec.setIp(hipconfig);
        return hvnicspec;
    }

    /**
     * @param hostmor reference to the host we're working with
     * @return ManagedObjectReference spec for the virtual machine
     */
    Object getHostConfigManagerrByHostMor(
            ManagedObjectReference hostmor) throws RuntimeFaultFaultMsg, InvocationTargetException, InvalidPropertyFaultMsg, NoSuchMethodException, IllegalAccessException {
        return getDynamicProperty(hostmor, "configManager");
    }


    void addVirtualNIC() throws HostConfigFaultFaultMsg, AlreadyExistsFaultMsg, InvalidStateFaultMsg, InvalidPropertyFaultMsg, InvocationTargetException, NoSuchMethodException, IllegalAccessException, RuntimeFaultFaultMsg {
        ManagedObjectReference dcmor;
        ManagedObjectReference hostfoldermor;
        ManagedObjectReference hostmor = null;

        if (((datacentername != null) && (hostname != null))
                || ((datacentername != null) && (hostname == null))) {
            dcmor = getDatacenterByName(datacentername);
            if (dcmor == null) {
                System.out.println("Datacenter not found");
                return;
            }
            hostfoldermor = getMorFromPropertyName(dcmor, "hostFolder");
            hostmor = getHost(hostfoldermor, hostname);
        } else if ((datacentername == null) && (hostname != null)) {
            hostmor = getHostByHostName(hostname);
        }
        if (hostmor != null) {
            Object cmobj = getHostConfigManagerrByHostMor(hostmor);
            HostConfigManager configMgr = (HostConfigManager) cmobj;
            ManagedObjectReference nwSystem = configMgr.getNetworkSystem();
            HostPortGroupSpec portgrp = new HostPortGroupSpec();
            portgrp.setName(portgroupname);

            HostVirtualNicSpec vNicSpec = createVirtualNicSpecification();
            String nic = vimPort.addVirtualNic(nwSystem, portgroupname, vNicSpec);

            System.out.println("Successful in creating nic : " + nic
                    + " with PortGroup :" + portgroupname);
        } else {
            System.out.println("Host not found");
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, AlreadyExistsFaultMsg, InvalidStateFaultMsg, InvocationTargetException, InvalidPropertyFaultMsg, NoSuchMethodException, IllegalAccessException, HostConfigFaultFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        rootFolder = serviceContent.getRootFolder();
        addVirtualNIC();
    }
}
