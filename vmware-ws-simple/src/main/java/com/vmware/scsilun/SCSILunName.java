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

package com.vmware.scsilun;

import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * SCSILunName
 *
 * This sample displays the CanonicalName,Vendor,Model,Data,Namespace
 * and NamespaceId of the host SCSI LUN name
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 * hostname       [required] : host for which SCSI details will be displayed
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.scsilun.SCSILunName
 * --url [webserviceurl] --username [username] --password [password]
 * --hostname [host name]
 * </pre>
 */
@Sample(
        name = "scsi-lun",
        description = "This sample displays the CanonicalName,Vendor,Model,Data,Namespace " +
                "and NamespaceId of the host SCSI LUN name"
)
public class SCSILunName extends ConnectedVimServiceBase {

    ManagedObjectReference propCollectorRef;
    ManagedObjectReference host;

    private String hostname;

    @Option(name = "hostname", description = "host for which SCSI details will be displayed")
    public void setHostname(String hostname) {
        this.hostname = hostname;
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
     * @param hostname :
     * @return
     */
    ManagedObjectReference getHostByHostName(String hostname) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
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
                if (hostnm != null && hostnm.equals(hostname)) {
                    retVal = mr;
                    break;
                }
            }
        } else {
            System.out.println("The Object Content is Null");
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
        hToVm.getSelectSet().add(getSelectionSpec("VisitFolders"));
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

        TraversalSpec crToRp = new TraversalSpec();
        crToRp.setType("ComputeResource");
        crToRp.setPath("resourcePool");
        crToRp.setSkip(Boolean.FALSE);
        crToRp.setName("crToRp");
        crToRp.getSelectSet().add(getSelectionSpec("rpToRp"));

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
        dcToHf.getSelectSet().add(getSelectionSpec("VisitFolders"));

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
        dcToVmf.getSelectSet().add(getSelectionSpec("VisitFolders"));

        // For Folder -> Folder recursion
        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");
        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(getSelectionSpec("crToRp"));
        sspecarrvf.add(getSelectionSpec("crToH"));
        sspecarrvf.add(getSelectionSpec("dcToVmf"));
        sspecarrvf.add(getSelectionSpec("dcToHf"));
        sspecarrvf.add(getSelectionSpec("vAppToRp"));
        sspecarrvf.add(getSelectionSpec("vAppToVM"));
        sspecarrvf.add(getSelectionSpec("dcToDs"));
        sspecarrvf.add(getSelectionSpec("hToVm"));
        sspecarrvf.add(getSelectionSpec("rpToVm"));
        sspecarrvf.add(getSelectionSpec("VisitFolders"));

        visitFolders.getSelectSet().addAll(sspecarrvf);

        List<SelectionSpec> resultspec = new ArrayList<SelectionSpec>();
        resultspec.add(visitFolders);
        resultspec.add(crToRp);
        resultspec.add(crToH);
        resultspec.add(dcToVmf);
        resultspec.add(dcToHf);
        resultspec.add(vAppToRp);
        resultspec.add(vAppToVM);
        resultspec.add(dcToDs);
        resultspec.add(hToVm);
        resultspec.add(rpToVm);
        resultspec.add(rpToRp);

        return resultspec;
    }

    SelectionSpec getSelectionSpec(String name) {
        SelectionSpec genericSpec = new SelectionSpec();
        genericSpec.setName(name);
        return genericSpec;
    }

    List<DynamicProperty> getDynamicPropArray(
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
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        ObjectContent contentObj = listobjcont.get(0);
        List<DynamicProperty> objList = contentObj.getPropSet();
        return objList;
    }

    /*
    * This subroutine prints the virtual machine file
    * system volumes affected by the given SCSI LUN.
    * @param  hmor      A HostSystem object of the given host.
    * @param canName    Canonical name of the SCSI logical unit
    */
    void getVMFS(ManagedObjectReference hmor, String canName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<DynamicProperty> dsArr =
                getDynamicPropArray(host, "HostSystem", "datastore");
        ArrayOfManagedObjectReference ds =
                ((ArrayOfManagedObjectReference) (dsArr.get(0)).getVal());
        List<ManagedObjectReference> dataStoresMOR =
                ds.getManagedObjectReference();
        boolean vmfsFlag = false;
        for (int j = 0; j < dataStoresMOR.size(); j++) {
            List<DynamicProperty> infoArr =
                    getDynamicPropArray(dataStoresMOR.get(j), "Datastore", "info");
            String infoClass = infoArr.get(0).getVal().getClass().toString();
            if (infoClass.equals("class com.vmware.vim.VmfsDatastoreInfo")) {
                VmfsDatastoreInfo vds =
                        (VmfsDatastoreInfo) infoArr.get(0).getVal();
                HostVmfsVolume hvms = vds.getVmfs();
                String vmfsName = vds.getName();
                List<HostScsiDiskPartition> hdp = hvms.getExtent();
                for (int k = 0; k < hdp.size(); k++) {
                    if (hdp.get(k).getDiskName().equals(canName)) {
                        vmfsFlag = true;
                        System.out.println(" " + vmfsName + "\n");
                    }
                }
            }
        }
        if (!vmfsFlag) {
            System.out.println(" None\n");
        }
    }

    /*
    *This subroutine prints the virtual machine
    *affected by the given SCSI LUN.
    *@param  hmor      ManagedObjectReference of the host
    *@param  canName   Canonical name of the SCSI logical unit
    */
    void getVMs(ManagedObjectReference hmor, String canName) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<DynamicProperty> dsArr =
                getDynamicPropArray(host, "HostSystem", "datastore");
        ArrayOfManagedObjectReference ds =
                ((ArrayOfManagedObjectReference) (dsArr.get(0)).getVal());
        List<ManagedObjectReference> dataStoresMOR =
                ds.getManagedObjectReference();
        boolean vmfsFlag = false;
        for (int j = 0; j < dataStoresMOR.size(); j++) {
            List<DynamicProperty> infoArr =
                    getDynamicPropArray(dataStoresMOR.get(j), "Datastore", "info");
            String infoClass = infoArr.get(0).getVal().getClass().toString();
            if (infoClass.equals("class com.vmware.vim.VmfsDatastoreInfo")) {
                VmfsDatastoreInfo vds =
                        (VmfsDatastoreInfo) infoArr.get(0).getVal();
                HostVmfsVolume hvms = vds.getVmfs();
                List<HostScsiDiskPartition> hdp = hvms.getExtent();
                for (int k = 0; k < hdp.size(); k++) {
                    if (hdp.get(k).getDiskName().equals(canName)) {
                        List<DynamicProperty> vmArr =
                                getDynamicPropArray(dataStoresMOR.get(j),
                                        "Datastore", "vm");
                        ArrayOfManagedObjectReference vms =
                                ((ArrayOfManagedObjectReference) vmArr.get(0)
                                        .getVal());
                        List<ManagedObjectReference> vmsMOR =
                                vms.getManagedObjectReference();
                        if (vmsMOR != null) {
                            for (int l = 0; l < vmsMOR.size(); l++) {
                                vmfsFlag = true;
                                List<DynamicProperty> nameArr =
                                        getDynamicPropArray(vmsMOR.get(l),
                                                "VirtualMachine", "name");
                                String vmname = nameArr.get(0).getVal().toString();
                                System.out.println(" " + vmname);
                            }
                        }
                    }
                }
            }
        }
        if (!vmfsFlag) {
            System.out.println(" None\n");
        }
    }

    public void printLunInfo(ManagedObjectReference host) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        List<DynamicProperty> scsiArry =
                getDynamicPropArray(host, "HostSystem",
                        "config.storageDevice.scsiLun");
        ArrayOfScsiLun scsiLUNarr =
                ((ArrayOfScsiLun) (scsiArry.get(0)).getVal());
        List<ScsiLun> scsiLun = scsiLUNarr.getScsiLun();
        if (scsiLun != null && scsiLun.size() > 0) {
            for (int j = 0; j < scsiLun.size(); j++) {
                System.out.println("\nSCSI LUN " + (j + 1));
                System.out.println("--------------");
                String canName = scsiLun.get(j).getCanonicalName();
                if (scsiLun.get(j).getDurableName() != null) {
                    ScsiLunDurableName scsiLunDurableName =
                            scsiLun.get(j).getDurableName();
                    List<Byte> data = scsiLunDurableName.getData();
                    String namespace = scsiLunDurableName.getNamespace();
                    byte namespaceId = scsiLunDurableName.getNamespaceId();
                    System.out.print("\nData            : ");
                    for (int i = 0; i < data.size(); i++) {
                        System.out.print(data.get(i) + " ");
                    }
                    System.out.println("\nCanonical Name  : " + canName);
                    System.out.println("Namespace       : " + namespace);
                    System.out.println("Namespace ID    : " + namespaceId);
                    System.out.println("\nVMFS Affected ");
                    getVMFS(host, canName);
                    System.out.println("Virtual Machines ");
                    getVMs(host, canName);
                } else {
                    System.out
                            .println("\nDurable name for "
                                    + scsiLun.get(j).getCanonicalName()
                                    + " does not exist");
                }
            }
        }
    }

    @com.vmware.common.annotations.Action
    public void run() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        host = getHostByHostName(hostname);
        printLunInfo(host);
    }
}
