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

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.*;

/**
 * <pre>
 * VMRelocate
 *
 * Used to relocate a linked clone using disk move type
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * vmname         [required] : name of the virtual machine
 * diskmovetype   [required] : Either of
 *                               [moveChildMostDiskBacking | moveAllDiskBackingsAndAllowSharing]
 * datastorename  [required] : Name of the datastore
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.VMRelocate --url [URLString] --username [User] --password [Password]
 * --vmname [VMName] --diskmovetype [DiskMoveType] --datastorename [Datastore]
 * </pre>
 */
@Sample(
        name = "vm-relocate",
        description = "Used to relocate a linked clone using disk move type"
)
public class VMRelocate extends ConnectedVimServiceBase {
    static final String[] diskMoveTypes = {"moveChildMostDiskBacking", "moveAllDiskBackingsAndAllowSharing"};
    private ManagedObjectReference propCollectorRef;

    String vmname = null;
    String diskMoveType = null;
    String datastoreName = null;

    @Option(name = "vmname", description = "name of the virtual machine")
    public void setVmname(String vmname) {
        this.vmname = vmname;
    }

    @Option(
            name = "diskmovetype",
            description = "Either of\n" +
                    "[moveChildMostDiskBacking | moveAllDiskBackingsAndAllowSharing]"
    )
    public void setDiskMoveType(String type) {
        check(type, diskMoveTypes);
        this.diskMoveType = type;
    }

    @Option(name = "datastorename", description = "Name of the datastore")
    public void setDatastoreName(String name) {
        this.datastoreName = name;
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

    /**
     * @return TraversalSpec specification to get to the VirtualMachine managed
     *         object.
     */
    TraversalSpec getVMTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the VirtualMachines.
        // Build the traversal specs bottoms up

        //Traversal to get to the VM in a VApp
        TraversalSpec vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        //Traversal spec for VApp to VApp
        TraversalSpec vAppToVApp = new TraversalSpec();
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("resourcePool");
        //SelectionSpec for VApp to VApp recursion
        SelectionSpec vAppRecursion = new SelectionSpec();
        vAppRecursion.setName("vAppToVApp");
        //SelectionSpec to get to a VM in the VApp
        SelectionSpec vmInVApp = new SelectionSpec();
        vmInVApp.setName("vAppToVM");
        //SelectionSpec for both VApp to VApp and VApp to VM
        List<SelectionSpec> vAppToVMSS = new ArrayList<SelectionSpec>();
        vAppToVMSS.add(vAppRecursion);
        vAppToVMSS.add(vmInVApp);
        vAppToVApp.getSelectSet().addAll(vAppToVMSS);

        //This SelectionSpec is used for recursion for Folder recursion
        SelectionSpec sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        // Traversal to get to the vmFolder from DataCenter
        TraversalSpec dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        dataCenterToVMFolder.getSelectSet().add(sSpec);

        // TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);
        List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(sSpec);
        sSpecArr.add(dataCenterToVMFolder);
        sSpecArr.add(vAppToVM);
        sSpecArr.add(vAppToVApp);
        traversalSpec.getSelectSet().addAll(sSpecArr);
        return traversalSpec;
    }

    /**
     * Get the MOR of the Virtual Machine by its name.
     *
     * @return The Managed Object reference for this VM
     */
    ManagedObjectReference getVmByVMname(String vmname) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ManagedObjectReference retVal = null;
        TraversalSpec tSpec = getVMTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setAll(Boolean.FALSE);
        propertySpec.getPathSet().add("name");
        propertySpec.setType("VirtualMachine");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(rootRef);
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
        List<ObjectContent> listobcont = retrievePropertiesAllObjects(listpfs);

        if (listobcont != null) {
            for (ObjectContent oc : listobcont) {
                ManagedObjectReference mr = oc.getObj();
                String vmnm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        vmnm = (String) dp.getVal();
                    }
                }
                if (vmnm != null && vmnm.equals(vmname)) {
                    retVal = mr;
                    break;
                }
            }
        }
        return retVal;
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

    /**
     * This code takes an array of [typename, property, property, ...] and
     * converts it into a PropertySpec[]. handles case where multiple references
     * to the same typename are specified.
     *
     * @param typeinfo 2D array of type and properties to retrieve
     * @return Array of container filter specs
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<PropertySpec> buildPropertySpecArray(String[][] typeinfo) {
        // Eliminate duplicates
        HashMap<String, Set> tInfo = new HashMap<String, Set>();
        for (int ti = 0; ti < typeinfo.length; ++ti) {
            Set<String> props = tInfo.get(typeinfo[ti][0]);
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
        List<PropertySpec> pSpecs = new ArrayList<PropertySpec>();
        for (Iterator<String> ki = tInfo.keySet().iterator(); ki.hasNext(); ) {
            String type = ki.next();
            PropertySpec pSpec = new PropertySpec();
            Set<?> props = tInfo.get(type);
            pSpec.setType(type);
            pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
            for (Iterator<?> pi = props.iterator(); pi.hasNext(); ) {
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
        List<PropertyFilterSpec> propertyFilterSpecList =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecList.add(spec);
        List<ObjectContent> retoc =
                retrievePropertiesAllObjects(propertyFilterSpecList);

        return retoc;
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

    String getProp(ManagedObjectReference obj, String prop) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String propVal = null;
        List<DynamicProperty> dynaProArray =
                getDynamicProarray(obj, obj.getType().toString(), prop);
        propVal = (String) dynaProArray.get(0).getVal();
        return propVal;
    }

    List<ManagedObjectReference> filterMOR(
            List<ManagedObjectReference> mors, String[][] filter) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<ManagedObjectReference> filteredmors =
                new ArrayList<ManagedObjectReference>();
        for (int i = 0; i < mors.size(); i++) {
            for (int k = 0; k < filter.length; k++) {
                String prop = filter[k][0];
                String reqVal = filter[k][1];
                String value = getProp((mors.get(i)), prop);
                if (reqVal == null) {
                    continue;
                } else if (value == null && reqVal != null) {
                    k = filter.length + 1;
                } else if (value != null && value.equalsIgnoreCase(reqVal)) {
                    filteredmors.add(mors.get(i));
                } else {
                    k = filter.length + 1;
                }
            }
        }
        return filteredmors;
    }

    List<ManagedObjectReference> getDecendentMoRefs(
            ManagedObjectReference root, String type, String[][] filter) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        String[][] typeinfo = new String[][]{new String[]{type, "name"},};

        List<ObjectContent> ocary =
                getContentsRecursively(null, root, typeinfo, true);

        List<ManagedObjectReference> refs =
                new ArrayList<ManagedObjectReference>();

        if (ocary == null || ocary.size() == 0) {
            return refs;
        }

        for (int oci = 0; oci < ocary.size(); oci++) {
            refs.add(ocary.get(oci).getObj());
        }

        if (filter != null) {
            List<ManagedObjectReference> filtermors = filterMOR(refs, filter);
            return filtermors;
        } else {
            return refs;
        }
    }

    List<ManagedObjectReference> getDecendentMoRefs(
            ManagedObjectReference root, String type) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<ManagedObjectReference> mors = getDecendentMoRefs(root, type, null);
        return mors;
    }

    DatastoreSummary getDataStoreSummary(
            ManagedObjectReference dataStore) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
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
        List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
        listPfs.add(propertyFilterSpec);
        List<ObjectContent> oContList = retrievePropertiesAllObjects(listPfs);
        for (int j = 0; j < oContList.size(); j++) {
            List<DynamicProperty> propSetList = oContList.get(j).getPropSet();
            for (int k = 0; k < propSetList.size(); k++) {
                dataStoreSummary = (DatastoreSummary) propSetList.get(k).getVal();
            }
        }
        return dataStoreSummary;
    }

    void relocate() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, VmConfigFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidDatastoreFaultMsg, FileFaultFaultMsg, MigrationFaultFaultMsg, InvalidStateFaultMsg, TimedoutFaultMsg, InvalidCollectorVersionFaultMsg {
        //get vm by vmname
        ManagedObjectReference vmMOR = getVmByVMname(vmname);

        ManagedObjectReference dsMOR = null;
        List<ManagedObjectReference> dcmors =
                getDecendentMoRefs(null, "Datacenter");
        Iterator<ManagedObjectReference> it = dcmors.iterator();
        while (it.hasNext()) {

            List<DynamicProperty> datastoresDPList =
                    getDynamicProarray(it.next(), "Datacenter", "datastore");
            ArrayOfManagedObjectReference dsMOArray =
                    (ArrayOfManagedObjectReference) datastoresDPList.get(0).getVal();
            List<ManagedObjectReference> datastores =
                    dsMOArray.getManagedObjectReference();
            boolean found = false;
            if (datastores != null) {
                for (int j = 0; j < datastores.size(); j++) {
                    DatastoreSummary ds = getDataStoreSummary(datastores.get(j));
                    String name = ds.getName();
                    if (name.equalsIgnoreCase(datastoreName)) {
                        dsMOR = datastores.get(j);
                        found = true;
                        j = datastores.size() + 1;
                    }
                }
            }
            if (found) {
                break;
            }
        }

        if (dsMOR == null) {
            System.out.println("Datastore " + datastoreName + " Not Found");
            return;
        }

        if (vmMOR != null) {
            VirtualMachineRelocateSpec rSpec = new VirtualMachineRelocateSpec();
            String moveType = diskMoveType;
            if (moveType.equalsIgnoreCase("moveChildMostDiskBacking")) {
                rSpec.setDiskMoveType("moveChildMostDiskBacking");
            } else if (moveType
                    .equalsIgnoreCase("moveAllDiskBackingsAndAllowSharing")) {
                rSpec.setDiskMoveType("moveAllDiskBackingsAndAllowSharing");
            }
            rSpec.setDatastore(dsMOR);
            ManagedObjectReference taskMOR =
                    vimPort.relocateVMTask(vmMOR, rSpec, null);
            if (getTaskResultAfterDone(taskMOR)) {
                System.out.println("Linked Clone relocated successfully.");
            } else {
                System.out.println("Failure -: Linked clone "
                        + "cannot be relocated");
            }
        } else {
            System.out.println("Virtual Machine " + vmname + " doesn't exist");
        }
    }

    boolean customValidation() {
        boolean flag = true;
        String val = diskMoveType;
        if ((!val.equalsIgnoreCase("moveChildMostDiskBacking"))
                && (!val.equalsIgnoreCase("moveAllDiskBackingsAndAllowSharing"))) {
            System.out
                    .println("diskmovetype option must be either moveChildMostDiskBacking or "
                            + "moveAllDiskBackingsAndAllowSharing");
            flag = false;
        }
        return flag;
    }

    boolean check(String value, String[] values) {
        boolean found = false;
        for (String v : values) {
            if (v.equals(value)) {
                found = true;
            }
        }
        return found;
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InsufficientResourcesFaultFaultMsg, VmConfigFaultFaultMsg, InvalidDatastoreFaultMsg, InvalidPropertyFaultMsg, FileFaultFaultMsg, InvalidStateFaultMsg, MigrationFaultFaultMsg, InvalidCollectorVersionFaultMsg, TimedoutFaultMsg {
        customValidation();
        propCollectorRef = serviceContent.getPropertyCollector();
        relocate();
    }
}
