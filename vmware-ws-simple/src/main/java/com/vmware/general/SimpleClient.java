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

package com.vmware.general;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * SimpleClient
 *
 * This sample lists the inventory contents (managed entities)
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.SimpleClient
 * --url [webserviceurl] --username [username] --password [password]
 * </pre>
 */

@Sample(name = "simple-client", description = "This sample lists the inventory contents (managed entities)")
public class SimpleClient extends ConnectedVimServiceBase {

    private ManagedObjectReference propCollectorRef;

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

    void getAndPrintInventoryContents() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        TraversalSpec resourcePoolTraversalSpec = new TraversalSpec();
        resourcePoolTraversalSpec.setName("resourcePoolTraversalSpec");
        resourcePoolTraversalSpec.setType("ResourcePool");
        resourcePoolTraversalSpec.setPath("resourcePool");
        resourcePoolTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec rpts = new SelectionSpec();
        rpts.setName("resourcePoolTraversalSpec");
        resourcePoolTraversalSpec.getSelectSet().add(rpts);

        TraversalSpec computeResourceRpTraversalSpec = new TraversalSpec();
        computeResourceRpTraversalSpec.setName("computeResourceRpTraversalSpec");
        computeResourceRpTraversalSpec.setType("ComputeResource");
        computeResourceRpTraversalSpec.setPath("resourcePool");
        computeResourceRpTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec rptss = new SelectionSpec();
        rptss.setName("resourcePoolTraversalSpec");
        computeResourceRpTraversalSpec.getSelectSet().add(rptss);

        TraversalSpec computeResourceHostTraversalSpec = new TraversalSpec();
        computeResourceHostTraversalSpec
                .setName("computeResourceHostTraversalSpec");
        computeResourceHostTraversalSpec.setType("ComputeResource");
        computeResourceHostTraversalSpec.setPath("host");
        computeResourceHostTraversalSpec.setSkip(Boolean.FALSE);

        TraversalSpec datacenterHostTraversalSpec = new TraversalSpec();
        datacenterHostTraversalSpec.setName("datacenterHostTraversalSpec");
        datacenterHostTraversalSpec.setType("Datacenter");
        datacenterHostTraversalSpec.setPath("hostFolder");
        datacenterHostTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftspec = new SelectionSpec();
        ftspec.setName("folderTraversalSpec");
        datacenterHostTraversalSpec.getSelectSet().add(ftspec);

        TraversalSpec datacenterVmTraversalSpec = new TraversalSpec();
        datacenterVmTraversalSpec.setName("datacenterVmTraversalSpec");
        datacenterVmTraversalSpec.setType("Datacenter");
        datacenterVmTraversalSpec.setPath("vmFolder");
        datacenterVmTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftspecs = new SelectionSpec();
        ftspecs.setName("folderTraversalSpec");
        datacenterVmTraversalSpec.getSelectSet().add(ftspecs);

        TraversalSpec folderTraversalSpec = new TraversalSpec();
        folderTraversalSpec.setName("folderTraversalSpec");
        folderTraversalSpec.setType("Folder");
        folderTraversalSpec.setPath("childEntity");
        folderTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftrspec = new SelectionSpec();
        ftrspec.setName("folderTraversalSpec");
        List<SelectionSpec> ssarray = new ArrayList<SelectionSpec>();
        ssarray.add(ftrspec);
        ssarray.add(datacenterHostTraversalSpec);
        ssarray.add(datacenterVmTraversalSpec);
        ssarray.add(computeResourceRpTraversalSpec);
        ssarray.add(computeResourceHostTraversalSpec);
        ssarray.add(resourcePoolTraversalSpec);

        folderTraversalSpec.getSelectSet().addAll(ssarray);
        PropertySpec props = new PropertySpec();
        props.setAll(Boolean.FALSE);
        props.getPathSet().add("name");
        props.setType("ManagedEntity");
        List<PropertySpec> propspecary = new ArrayList<PropertySpec>();
        propspecary.add(props);

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().addAll(propspecary);

        spec.getObjectSet().add(new ObjectSpec());
        spec.getObjectSet().get(0).setObj(rootRef);
        spec.getObjectSet().get(0).setSkip(Boolean.FALSE);
        spec.getObjectSet().get(0).getSelectSet().add(folderTraversalSpec);

        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

        // If we get contents back. print them out.
        if (listobjcont != null) {
            ObjectContent oc = null;
            ManagedObjectReference mor = null;
            DynamicProperty pc = null;
            for (int oci = 0; oci < listobjcont.size(); oci++) {
                oc = listobjcont.get(oci);
                mor = oc.getObj();

                List<DynamicProperty> listdp = oc.getPropSet();
                System.out.println("Object Type : " + mor.getType());
                System.out.println("Reference Value : " + mor.getValue());

                if (listdp != null) {
                    for (int pci = 0; pci < listdp.size(); pci++) {
                        pc = listdp.get(pci);
                        System.out.println("   Property Name : " + pc.getName());
                        if (pc != null) {
                            if (!pc.getVal().getClass().isArray()) {
                                System.out
                                        .println("   Property Value : " + pc.getVal());
                            } else {
                                List<Object> ipcary = new ArrayList<Object>();
                                ipcary.add(pc.getVal());
                                System.out.println("Val : " + pc.getVal());
                                for (int ii = 0; ii < ipcary.size(); ii++) {
                                    Object oval = ipcary.get(ii);
                                    if (oval.getClass().getName()
                                            .indexOf("ManagedObjectReference") >= 0) {
                                        ManagedObjectReference imor =
                                                (ManagedObjectReference) oval;

                                        System.out.println("Inner Object Type : "
                                                + imor.getType());
                                        System.out.println("Inner Reference Value : "
                                                + imor.getValue());
                                    } else {
                                        System.out.println("Inner Property Value : "
                                                + oval);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No Managed Entities retrieved!");
        }
    }

    @Action
    public void main() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        getAndPrintInventoryContents();
    }
}
