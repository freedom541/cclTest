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

package com.vmware.vm;

import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.*;

/**
 * <pre>
 * VMotion
 *
 * Used to validate if VMotion is feasible between two hosts or not,
 * It is also used to perform migrate/relocate task depending on the data given
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : name of the virtual machine
 * targethost     [required] : Name of the target host
 * sourcehost     [required] : Name of the host containing the virtual machine
 * targetpool     [required] : Name of the target resource pool
 * targetdatastore [required] : Name of the target datastore
 * priority       [required] : The priority of the migration task:-
 *                             default_Priority, high_Priority,low_Priority
 * state          [optional]
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMotion --url [URLString] --username [User] --password [Password]
 * --vmname [VMName] --targethost [Target host] --sourcehost [Source host] --targetpool [Target resource pool]
 * --targetdatastore [Target datastore] --priority [Migration task priority] --state
 * </pre>
 */
@Sample(
        name = "vmotion",
        description = "Used to validate if VMotion is feasible between two hosts or not,\n" +
                "It is also used to perform migrate/relocate task depending on the data given"
)
public class VMotion extends ConnectedVimServiceBase {

    String[] meTree = {"ManagedEntity", "ComputeResource",
            "ClusterComputeResource", "Datacenter", "Folder", "HostSystem",
            "ResourcePool", "VirtualMachine"};
    String[] crTree = {"ComputeResource",
            "ClusterComputeResource"};
    String[] hcTree = {"HistoryCollector",
            "EventHistoryCollector", "TaskHistoryCollector"};

    ManagedObjectReference propCollectorRef = null;

    /*
   Connection input parameters
    */
    String vmName = null;
    String targetHost = null;
    String targetPool = null;
    String sourceHost = null;
    String targetDS = null;
    String priority = null;
    String state = null;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVmName(String name) {
        this.vmName = name;
    }

    @Option(name = "targethost", description = "Name of the target host")
    public void setTargetHost(String host) {
        this.targetHost = host;
    }

    @Option(name = "sourcehost", description = "Name of the host containing the virtual machine")
    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    @Option(name = "targetpool", description = "Name of the target resource pool")
    public void setTargetPool(String targetPool) {
        this.targetPool = targetPool;
    }

    @Option(name = "targetdatastore", description = "Name of the target datastore")
    public void setTargetDS(String targetDS) {
        this.targetDS = targetDS;
    }

    @Option(name = "priority", description = "The priority of the migration task: " +
            "default_Priority, high_Priority,low_Priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Option(name = "state", required = false)
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
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

    /*
    * @return An array of SelectionSpec covering VM, Host, Resource pool,
    * Cluster Compute Resource and Datastore.
    */
    List<SelectionSpec> buildFullTraversal() {
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
        hToVm.setName("hToVm");
        hToVm.getSelectSet().add(getSelectionSpec("visitFolders"));
        hToVm.setSkip(Boolean.FALSE);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        // Recurse through all ResourcePools
        TraversalSpec rpToRp = new TraversalSpec();
        rpToRp.setType("ResourcePool");
        rpToRp.setPath("resourcePool");
        rpToRp.setSkip(Boolean.FALSE);
        rpToRp.setName("rpToRp");
        rpToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
        rpToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));
        crToRp.getSelectSet().add(getSelectionSpec("rpToVm"));

        TraversalSpec crToH = new TraversalSpec();
        crToH.setSkip(Boolean.FALSE);
        crToH.setType("ComputeResource");
        crToH.setPath("host");
        crToH.setName("crToH");

        TraversalSpec dcToHf = new TraversalSpec();
        dcToHf.setSkip(Boolean.FALSE);
        dcToHf.setType("Datacenter");
        dcToHf.setPath("hostFolder");
        dcToHf.setName("dcToHf");
        dcToHf.getSelectSet().add(getSelectionSpec("visitFolders"));

        TraversalSpec vAppToRp = new TraversalSpec();
        vAppToRp.setName("vAppToRp");
        vAppToRp.setType("VirtualApp");
        vAppToRp.setPath("resourcePool");
        vAppToRp.getSelectSet().add(getSelectionSpec("rpToRp"));

        TraversalSpec dcToVmf = new TraversalSpec();
        dcToVmf.setType("Datacenter");
        dcToVmf.setSkip(Boolean.FALSE);
        dcToVmf.setPath("vmFolder");
        dcToVmf.setName("dcToVmf");
        dcToVmf.getSelectSet().add(getSelectionSpec("visitFolders"));

        // For Folder -> Folder recursion
        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("visitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(getSelectionSpec("visitFolders"));
        sspecarrvf.add(getSelectionSpec("dcToVmf"));
        sspecarrvf.add(getSelectionSpec("dcToHf"));
        sspecarrvf.add(getSelectionSpec("dcToDs"));
        sspecarrvf.add(getSelectionSpec("crToRp"));
        sspecarrvf.add(getSelectionSpec("crToH"));
        sspecarrvf.add(getSelectionSpec("hToVm"));
        sspecarrvf.add(getSelectionSpec("rpToVm"));
        sspecarrvf.add(getSelectionSpec("rpToRp"));
        sspecarrvf.add(getSelectionSpec("vAppToRp"));
        sspecarrvf.add(getSelectionSpec("vAppToVM"));

        visitFolders.getSelectSet().addAll(sspecarrvf);

        List<SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
        resultspec.add(visitFolders);
        resultspec.add(dcToVmf);
        resultspec.add(dcToHf);
        resultspec.add(dcToDs);
        resultspec.add(crToRp);
        resultspec.add(crToH);
        resultspec.add(hToVm);
        resultspec.add(rpToVm);
        resultspec.add(vAppToRp);
        resultspec.add(vAppToVM);
        resultspec.add(rpToRp);

        return resultspec;
    }

    SelectionSpec getSelectionSpec(String name) {
        SelectionSpec genericSpec = new SelectionSpec();
        genericSpec.setName(name);
        return genericSpec;
    }

    List<DynamicProperty> getDynamicProarray(
            ManagedObjectReference ref, String type, String propertyString) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add(propertyString);
        propertySpec.setType(type);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(ref);
        objectSpec.setSkip(Boolean.FALSE);
        objectSpec.getSelectSet().addAll(buildFullTraversal());
        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);
        List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
        listPfs.add(propertyFilterSpec);
        List<ObjectContent> oContList = retrievePropertiesAllObjects(listPfs);
        ObjectContent contentObj = oContList.get(0);
        List<DynamicProperty> objList = contentObj.getPropSet();
        return objList;
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
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
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
        if (retVal == null) {
            throw new RuntimeException("Host " + hostName + " not found.");
        }
        return retVal;
    }

    DatastoreSummary getDataStoreSummary(
            ManagedObjectReference dataStore) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        DatastoreSummary dataStoreSummary = new DatastoreSummary();
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("summary");
        propertySpec.setType("Datastore");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(dataStore);
        objectSpec.setSkip(Boolean.FALSE);
        objectSpec.getSelectSet().addAll(buildFullTraversal());
        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        for (int j = 0; j < listobjcont.size(); j++) {
            List<DynamicProperty> propSetList = listobjcont.get(j).getPropSet();
            for (int k = 0; k < propSetList.size(); k++) {
                dataStoreSummary = (DatastoreSummary) propSetList.get(k).getVal();
            }
        }
        return dataStoreSummary;
    }

    ManagedObjectReference browseDSMOR(
            List<ManagedObjectReference> dsMOR) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference dataMOR = null;
        if (dsMOR != null && dsMOR.size() > 0) {
            for (int i = 0; i < dsMOR.size(); i++) {
                DatastoreSummary ds = getDataStoreSummary(dsMOR.get(i));
                String dsname = ds.getName();
                if (dsname.equalsIgnoreCase(targetDS)) {
                    dataMOR = dsMOR.get(i);
                    break;
                }
            }
        }
        return dataMOR;
    }

    /*
    *This function is used to check whether relocation is to be done or
    *migration is to be done. If two hosts have a shared datastore then
    *migration will be done and if there is no shared datastore relocation
    *will be done.
    *@param String name of the source host
    *@param String name of the target host
    *@param String name of the target datastore
    *@return String mentioning migration or relocation
    */
    String checkOperationType(String targetHost,
                              String sourceHost, String targetDS) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String operation = "";
        if (targetHost.equalsIgnoreCase(sourceHost)) {
            return "same";
        }
        ManagedObjectReference targetHostMOR = getHostByHostName(targetHost);
        ManagedObjectReference sourceHostMOR = getHostByHostName(sourceHost);
        List<DynamicProperty> datastoresTarget =
                getDynamicProarray(targetHostMOR, "HostSystem", "datastore");
        ArrayOfManagedObjectReference dsTargetArr =
                ((ArrayOfManagedObjectReference) (datastoresTarget.get(0)).getVal());
        List<ManagedObjectReference> dsTarget =
                dsTargetArr.getManagedObjectReference();
        ManagedObjectReference tarHostDS = browseDSMOR(dsTarget);
        List<DynamicProperty> datastoresSource =
                getDynamicProarray(sourceHostMOR, "HostSystem", "datastore");
        ArrayOfManagedObjectReference dsSourceArr =
                ((ArrayOfManagedObjectReference) (datastoresSource.get(0)).getVal());
        List<ManagedObjectReference> dsSourceList =
                dsSourceArr.getManagedObjectReference();
        ManagedObjectReference srcHostDS = browseDSMOR(dsSourceList);

        if ((tarHostDS != null) && (srcHostDS != null)) {
            operation = "migrate";
        } else {
            operation = "relocate";
        }
        return operation;
    }

    /**
     * This code takes an array of [typename, property, property, ...] and
     * converts it into a PropertySpec[]. handles case where multiple references
     * to the same typename are specified.
     *
     * @param typeinfo 2D array of type and properties to retrieve
     * @return Array of container filter specs
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
        // Eliminate duplicates
        HashMap<String, Set> tInfo = new HashMap<String, Set>();
        for (int ti = 0; ti < typeinfo.length; ++ti) {
            Set props = tInfo.get(typeinfo[ti][0]);
            if (props == null) {
                props = new HashSet<String>();
                tInfo.put(typeinfo[ti][0], props);
            }
            boolean typeSkipped = false;
            for (int pi = 0; pi < typeinfo[ti].length; ++pi) {
                String prop = typeinfo[ti][pi];
                if (typeSkipped) {
                    props.add(prop);
                } else {
                    typeSkipped = true;
                }
            }
        }

        // Create PropertySpecs
        ArrayList<PropertySpec> pSpecs = new ArrayList<PropertySpec>();
        for (Iterator<String> ki = tInfo.keySet().iterator(); ki.hasNext(); ) {
            String type = ki.next();
            PropertySpec pSpec = new PropertySpec();
            Set props = tInfo.get(type);
            pSpec.setType(type);
            pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
            for (Iterator pi = props.iterator(); pi.hasNext(); ) {
                String prop = (String) pi.next();
                pSpec.getPathSet().add(prop);
            }
            pSpecs.add(pSpec);
        }

        return pSpecs;
    }

    /**
     * Retrieve content recursively with multiple properties. the typeinfo array
     * contains typename + properties to retrieve.
     *
     * @param collector a property collector if available or null for default
     * @param root      a root folder if available, or null for default
     * @param typeinfo  2D array of properties for each typename
     * @param recurse   retrieve contents recursively from the root down
     * @return retrieved object contents
     */
    List<ObjectContent> getContentsRecursively(
            ManagedObjectReference collector, ManagedObjectReference root,
            String[][] typeinfo, boolean recurse) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        if (typeinfo == null || typeinfo.length == 0) {
            return null;
        }

        ManagedObjectReference usecoll = collector;
        if (usecoll == null) {
            usecoll = serviceContent.getPropertyCollector();
        }

        ManagedObjectReference useroot = root;
        if (useroot == null) {
            useroot = serviceContent.getRootFolder();
        }

        List<SelectionSpec> selectionSpecs = null;
        if (recurse) {
            selectionSpecs = buildFullTraversal();
        }

        List<PropertySpec> propspecary = buildPropertySpecArray(typeinfo);
        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(useroot);
        objSpec.setSkip(Boolean.FALSE);
        objSpec.getSelectSet().addAll(selectionSpecs);
        List<ObjectSpec> objSpecList = new ArrayList<ObjectSpec>();
        objSpecList.add(objSpec);
        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().addAll(propspecary);
        spec.getObjectSet().addAll(objSpecList);
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

        return listobjcont;
    }

    boolean typeIsA(String searchType, String foundType) {
        if (searchType.equals(foundType)) {
            return true;
        } else if (searchType.equals("ManagedEntity")) {
            for (int i = 0; i < meTree.length; ++i) {
                if (meTree[i].equals(foundType)) {
                    return true;
                }
            }
        } else if (searchType.equals("ComputeResource")) {
            for (int i = 0; i < crTree.length; ++i) {
                if (crTree[i].equals(foundType)) {
                    return true;
                }
            }
        } else if (searchType.equals("HistoryCollector")) {
            for (int i = 0; i < hcTree.length; ++i) {
                if (hcTree[i].equals(foundType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the ManagedObjectReference for an item under the specified root folder
     * that has the type and name specified.
     *
     * @param root a root folder if available, or null for default
     * @param type type of the managed object
     * @param name name to match
     * @return First ManagedObjectReference of the type / name pair found
     */
    ManagedObjectReference getDecendentMoRef(
            ManagedObjectReference root, String type, String name) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        if (name == null || name.length() == 0) {
            return null;
        }

        String[][] typeinfo = new String[][]{new String[]{type, "name"},};

        List<ObjectContent> ocary =
                getContentsRecursively(null, root, typeinfo, true);

        if (ocary == null || ocary.size() == 0) {
            return null;
        }

        ObjectContent oc = null;
        ManagedObjectReference mor = null;
        List<DynamicProperty> propary = null;
        String propval = null;
        boolean found = false;
        for (int oci = 0; oci < ocary.size() && !found; oci++) {
            oc = ocary.get(oci);
            mor = oc.getObj();
            propary = oc.getPropSet();

            propval = null;
            if (type == null || typeIsA(type, mor.getType())) {
                if (propary.size() > 0) {
                    propval = (String) propary.get(0).getVal();
                }
                found = propval != null && name.equals(propval);
            }
        }

        if (!found) {
            mor = null;
        }

        return mor;
    }

    void migrateVM(String vmname, String pool, String tHost,
                   String srcHost, String priority) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg, TimedoutFaultMsg, InvalidCollectorVersionFaultMsg {
        VirtualMachinePowerState st = null;
        VirtualMachineMovePriority pri = null;
        if (state != null) {
            if (VirtualMachinePowerState.POWERED_OFF.toString().equalsIgnoreCase(
                    state)) {
                st = VirtualMachinePowerState.POWERED_OFF;
            } else if (VirtualMachinePowerState.POWERED_ON.toString()
                    .equalsIgnoreCase(state)) {
                st = VirtualMachinePowerState.POWERED_ON;
            } else if (VirtualMachinePowerState.SUSPENDED.toString()
                    .equalsIgnoreCase(state)) {
                st = VirtualMachinePowerState.SUSPENDED;
            }
        }
        if (priority == null) {
            pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
        } else {
            if (VirtualMachineMovePriority.DEFAULT_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
            } else if (VirtualMachineMovePriority.HIGH_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.HIGH_PRIORITY;
            } else if (VirtualMachineMovePriority.LOW_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.LOW_PRIORITY;
            }
        }
        ManagedObjectReference srcMOR = getHostByHostName(srcHost);
        if (srcMOR == null) {
            throw new IllegalArgumentException("Source Host" + sourceHost
                    + " Not Found.");
        }
        //ManagedObjectReference vmMOR = getVmByVMname(vmname);
        ManagedObjectReference vmMOR =
                getDecendentMoRef(srcMOR, "VirtualMachine", vmname);
        if (vmMOR == null) {
            throw new IllegalArgumentException("Virtual Machine " + vmName
                    + " Not Found.");
        }
        ManagedObjectReference poolMOR =
                getDecendentMoRef(null, "ResourcePool", pool);
        if (poolMOR == null) {
            throw new IllegalArgumentException("Target Resource Pool "
                    + targetPool + " Not Found.");
        }
        ManagedObjectReference hMOR = getHostByHostName(tHost);
        if (hMOR == null) {
            throw new IllegalArgumentException(" Target Host " + targetHost
                    + " Not Found.");
        }
        System.out.println("Migrating the Virtual Machine " + vmname);
        ManagedObjectReference taskMOR =
                vimPort.migrateVMTask(vmMOR, poolMOR, hMOR, pri, st);
        if (getTaskResultAfterDone(taskMOR)) {
            System.out.println("Migration of Virtual Machine " + vmname
                    + " done successfully to " + tHost);
        } else {
            System.out.println("Error::  Migration failed");
        }
    }

    /*
    *This function is used for doing the relocation VM task
    *@param String vmname
    *@param String resourcepool name
    *@param String name of the target host
    *@param String name of the target datastore
    */
    void relocateVM(String vmname, String pool, String tHost,
                    String tDS, String srcHost) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg, TimedoutFaultMsg, InvalidCollectorVersionFaultMsg {
        VirtualMachineMovePriority pri = null;
        if (priority == null) {
            pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
        } else {
            if (VirtualMachineMovePriority.DEFAULT_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.DEFAULT_PRIORITY;
            } else if (VirtualMachineMovePriority.HIGH_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.HIGH_PRIORITY;
            } else if (VirtualMachineMovePriority.LOW_PRIORITY.toString()
                    .equalsIgnoreCase(priority)) {
                pri = VirtualMachineMovePriority.LOW_PRIORITY;
            }
        }
        ManagedObjectReference srcMOR = getHostByHostName(srcHost);
        if (srcMOR == null) {
            throw new IllegalArgumentException(" Source Host " + sourceHost
                    + " Not Found.");
        }
        ManagedObjectReference vmMOR =
                getDecendentMoRef(srcMOR, "VirtualMachine", vmname);
        if (vmMOR == null) {
            throw new IllegalArgumentException("Virtual Machine " + vmName
                    + " Not Found.");
        }
        ManagedObjectReference poolMOR =
                getDecendentMoRef(null, "ResourcePool", pool);
        if (poolMOR == null) {
            throw new IllegalArgumentException(" Target Resource Pool "
                    + targetPool + " Not Found.");
        }
        ManagedObjectReference hMOR = getHostByHostName(tHost);
        if (hMOR == null) {
            throw new IllegalArgumentException(" Target Host " + targetHost
                    + " Not Found.");
        }

        List<DynamicProperty> datastoresSource =
                getDynamicProarray(hMOR, "HostSystem", "datastore");
        ArrayOfManagedObjectReference dsSourceArr =
                ((ArrayOfManagedObjectReference) (datastoresSource.get(0)).getVal());
        List<ManagedObjectReference> dsTarget =
                dsSourceArr.getManagedObjectReference();
        ManagedObjectReference dsMOR = browseDSMOR(dsTarget);
        if (dsMOR == null) {
            throw new IllegalArgumentException(" DataSource " + tDS
                    + " Not Found.");
        }
        VirtualMachineRelocateSpec relSpec = new VirtualMachineRelocateSpec();
        relSpec.setDatastore(dsMOR);
        relSpec.setHost(hMOR);
        relSpec.setPool(poolMOR);
        System.out.println("Relocating the Virtual Machine " + vmname);
        ManagedObjectReference taskMOR =
                vimPort.relocateVMTask(vmMOR, relSpec, pri);

        if (getTaskResultAfterDone(taskMOR)) {
            System.out.println("Relocation done successfully of " + vmname
                    + " to host " + tHost);
        } else {
            System.out.println("Error::  Relocation failed");
        }
    }

    void migrateOrRelocateVM() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, MigrationFaultFaultMsg, InvalidCollectorVersionFaultMsg, TimedoutFaultMsg, InvalidDatastoreFaultMsg {
        // first we need to check if the VM should be migrated of relocated
        // If target host and source host both contains
        //the datastore, virtual machine needs to be migrated
        // If only target host contains the datastore, machine needs to be relocated
        String operationName =
                checkOperationType(targetHost, sourceHost, targetDS);
        if (operationName.equalsIgnoreCase("migrate")) {
            migrateVM(vmName, targetPool, targetHost, sourceHost, priority);
        } else if (operationName.equalsIgnoreCase("relocate")) {
            relocateVM(vmName, targetPool, targetHost, targetDS, sourceHost);
        } else if (operationName.equalsIgnoreCase("same")) {
            throw new IllegalArgumentException(
                    "targethost and sourcehost must not be same");
        } else {
            throw new IllegalArgumentException(operationName + " Not Found.");
        }
    }

    boolean customValidation() {
        boolean flag = true;
        if (state != null) {
            if (!state.equalsIgnoreCase("poweredOn")
                    && !state.equalsIgnoreCase("poweredOff")
                    && !state.equalsIgnoreCase("suspended")) {
                System.out.println("Must specify 'poweredOn', 'poweredOff' or"
                        + " 'suspended' for 'state' option\n");
                flag = false;
            }
        }
        if (priority != null) {
            if (!priority.equalsIgnoreCase("default_Priority")
                    && !priority.equalsIgnoreCase("high_Priority")
                    && !priority.equalsIgnoreCase("low_Priority")) {
                System.out
                        .println("Must specify 'default_Priority', 'high_Priority "
                                + " 'or 'low_Priority' for 'priority' option\n");
                flag = false;
            }
        }
        return flag;
    }


}
