/*
 * *****************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * *****************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.connection.helpers;

import com.vmware.connection.Connection;
import com.vmware.connection.helpers.builders.*;
import com.vmware.vim25.*;

import java.util.*;

public class GetMOREF extends BaseHelper {
    VimPortType vimPort;
    ServiceContent serviceContent;


    public GetMOREF(final Connection connection) {
        super(connection);
    }

    /**
     * Initialize the helper object on the current connection at invocation time. Do not initialize on construction
     * since the connection may not be ready yet.
     */
    private void init() {
        try {
            vimPort = connection.connect().getVimPort();
            serviceContent = connection.connect().getServiceContent();
        } catch (Throwable cause) {
            throw new HelperException(cause);
        }
    }

    public RetrieveResult containerViewByType(
            final ManagedObjectReference container,
            final String morefType,
            final RetrieveOptions retrieveOptions
    ) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        return this.containerViewByType(container,morefType,retrieveOptions,"name");
    }

    /**
     * Returns the raw RetrieveResult object for the provided container filtered on properties list
     *
     * @param container       - container to look in
     * @param morefType       - type to filter for
     * @param morefProperties - properties to include
     * @return com.vmware.vim25.RetrieveResult for this query
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public RetrieveResult containerViewByType(
            final ManagedObjectReference container,
            final String morefType,
            final RetrieveOptions retrieveOptions,
            final String... morefProperties
    ) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        init();

        PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(container, morefType, morefProperties);

        return containerViewByType(container,morefType,morefProperties,retrieveOptions,propertyFilterSpecs);
    }

    public PropertyFilterSpec[] propertyFilterSpecs(
            ManagedObjectReference container,
            String morefType,
            String... morefProperties
    ) throws RuntimeFaultFaultMsg {
        init();

        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView =
                vimPort.createContainerView(viewManager, container,
                        Arrays.asList(morefType), true);

        return new PropertyFilterSpec[]{
                new PropertyFilterSpecBuilder()
                        .propSet(
                                new PropertySpecBuilder()
                                        .all(Boolean.FALSE)
                                        .type(morefType)
                                        .pathSet(morefProperties)
                        )
                        .objectSet(
                                new ObjectSpecBuilder()
                                        .obj(containerView)
                                        .skip(Boolean.TRUE)
                                        .selectSet(
                                                new TraversalSpecBuilder()
                                                        .name("view")
                                                        .path("view")
                                                        .skip(false)
                                                        .type("ContainerView")
                                        )
                        )
        };
    }

    public RetrieveResult containerViewByType(
            final ManagedObjectReference container,
            final String morefType,
            final String[] morefProperties,
            final RetrieveOptions retrieveOptions,
            final PropertyFilterSpec... propertyFilterSpecs
    ) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        init();
        return vimPort.retrievePropertiesEx(
                serviceContent.getPropertyCollector(),
                Arrays.asList(propertyFilterSpecs),
                retrieveOptions
        );
    }

    /**
     * Returns all the MOREFs of the specified type that are present under the
     * folder
     *
     * @param folder    {@link ManagedObjectReference} of the folder to begin the search
     *                  from
     * @param morefType Type of the managed entity that needs to be searched
     * @return Map of name and MOREF of the managed objects present. If none
     *         exist then empty Map is returned
     * @throws InvalidPropertyFaultMsg
     *
     * @throws RuntimeFaultFaultMsg
     *
     */
    public Map<String, ManagedObjectReference> inFolderByType(
            final ManagedObjectReference folder, final String morefType, final RetrieveOptions retrieveOptions
    ) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        final PropertyFilterSpec[] propertyFilterSpecs = propertyFilterSpecs(folder, morefType, "name");

        // reuse this property collector again later to scroll through results
        final ManagedObjectReference propertyCollector = serviceContent.getPropertyCollector();

        RetrieveResult results = vimPort.retrievePropertiesEx(
                propertyCollector,
                Arrays.asList(propertyFilterSpecs),
                retrieveOptions);

        final Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();
        while(results != null && !results.getObjects().isEmpty()) {
            resultsToTgtMorefMap(results, tgtMoref);
            final String token = results.getToken();
            // if we have a token, we can scroll through additional results, else there's nothing to do.
            results =
                    (token != null) ?
                            vimPort.continueRetrievePropertiesEx(propertyCollector,token) : null;
        }

        return tgtMoref;
    }

    private void resultsToTgtMorefMap(RetrieveResult results, Map<String, ManagedObjectReference> tgtMoref) {
        List<ObjectContent> oCont = (results != null) ? results.getObjects() : null;

        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
    }


    /**
     * Returns all the MOREFs of the specified type that are present under the
     * container
     *
     * @param container       {@link ManagedObjectReference} of the container to begin the
     *                        search from
     * @param morefType       Type of the managed entity that needs to be searched
     * @param morefProperties Array of properties to be fetched for the moref
     * @return Map of MOREF and Map of name value pair of properties requested of
     *         the managed objects present. If none exist then empty Map is
     *         returned
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public Map<ManagedObjectReference, Map<String, Object>> inContainerByType(
            ManagedObjectReference container, String morefType,
            String[] morefProperties, RetrieveOptions retrieveOptions) throws InvalidPropertyFaultMsg,
            RuntimeFaultFaultMsg {
        List<ObjectContent> oCont = containerViewByType(container, morefType, retrieveOptions, morefProperties).getObjects();

        Map<ManagedObjectReference, Map<String, Object>> tgtMoref =
                new HashMap<ManagedObjectReference, Map<String, Object>>();

        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                Map<String, Object> propMap = new HashMap<String, Object>();
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        propMap.put(dp.getName(), dp.getVal());
                    }
                }
                tgtMoref.put(oc.getObj(), propMap);
            }
        }
        return tgtMoref;
    }

    /**
     * Returns all the MOREFs of the specified type that are present under the
     * container
     *
     * @param folder    {@link ManagedObjectReference} of the container to begin the
     *                  search from
     * @param morefType Type of the managed entity that needs to be searched
     * @return Map of name and MOREF of the managed objects present. If none
     *         exist then empty Map is returned
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, ManagedObjectReference> inContainerByType(
            ManagedObjectReference folder, String morefType, RetrieveOptions retrieveOptions)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        init();
        RetrieveResult rslts = containerViewByType(folder, morefType, retrieveOptions);
       return toMap(rslts);
    }

    public Map<String, ManagedObjectReference> toMap(RetrieveResult rslts) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        final Map<String, ManagedObjectReference> tgtMoref = new HashMap<String, ManagedObjectReference>();
        String token = null;
        token = populate(rslts, tgtMoref);

        while ( token != null && !token.isEmpty() ) {
            // fetch results based on new token
            rslts = vimPort.continueRetrievePropertiesEx(
                            serviceContent.getPropertyCollector(), token);

            token = populate(rslts, tgtMoref);
        }

        return tgtMoref;
    }

    public static String populate(final RetrieveResult rslts, final Map<String, ManagedObjectReference> tgtMoref) {
        String token = null;
        if (rslts != null) {
            token = rslts.getToken();
            for(ObjectContent oc : rslts.getObjects()) {
                ManagedObjectReference mr = oc.getObj();
                String entityNm = null;
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        entityNm = (String) dp.getVal();
                    }
                }
                tgtMoref.put(entityNm, mr);
            }
        }
        return token;
    }

    public static String populate(final RetrieveResult rslts, final List<ObjectContent> listobjcontent) {
        String token = null;
        if (rslts != null) {
            token = rslts.getToken();
            listobjcontent.addAll(rslts.getObjects());
        }
        return token;
    }

    /**
     * Get the MOR of the Virtual Machine by its name.
     *
     * @param vmName           The name of the Virtual Machine
     * @param propCollectorRef
     * @return The Managed Object reference for this VM
     * @throws RuntimeFaultFaultMsg
     * @throws InvalidPropertyFaultMsg
     */
    public ManagedObjectReference vmByVMname(
            final String vmName, final ManagedObjectReference propCollectorRef
    ) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        init();

        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        TraversalSpec tSpec = getVMTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpecBuilder()
            .all(Boolean.FALSE)
            .pathSet("name")
            .type("VirtualMachine");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpecBuilder()
            .obj(rootFolder)
            .skip(Boolean.TRUE)
            .selectSet(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder()
                .propSet(propertySpec)
                .objectSet(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);

        RetrieveOptions options = new RetrieveOptions();
        List<ObjectContent> listobcont =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options).getObjects();

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
                if (vmnm != null && vmnm.equals(vmName)) {
                    retVal = mr;
                    break;
                }
            }
        }
        return retVal;
    }
    public ManagedObjectReference virtual(
            final String name, String type, final ManagedObjectReference propCollectorRef
    ) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        init();

        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        TraversalSpec tSpec = getVMTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpecBuilder()
            .all(Boolean.FALSE)
            .pathSet("name")
            .type(type);

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpecBuilder()
            .obj(rootFolder)
            .skip(Boolean.TRUE)
            .selectSet(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder()
                .propSet(propertySpec)
                .objectSet(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);

        RetrieveOptions options = new RetrieveOptions();
        List<ObjectContent> listobcont =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options).getObjects();

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
                if (vmnm != null && vmnm.equals(name)) {
                    retVal = mr;
                    break;
                }
            }
        }
        return retVal;
    }


    public ManagedObjectReference vmByVMUUID(
            final String vmUUID, final ManagedObjectReference propCollectorRef
    ) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        init();

        ManagedObjectReference retVal = null;
        ManagedObjectReference rootFolder = serviceContent.getRootFolder();
        TraversalSpec tSpec = getVMTraversalSpec();
        // Create Property Spec
        PropertySpec propertySpec = new PropertySpecBuilder()
                .all(Boolean.FALSE)
                .pathSet("summary.config.instanceUuid")
                .type("VirtualMachine");

        // Now create Object Spec
        ObjectSpec objectSpec = new ObjectSpecBuilder()
                .obj(rootFolder)
                .skip(Boolean.TRUE)
                .selectSet(tSpec);

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        // created above.
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpecBuilder()
                .propSet(propertySpec)
                .objectSet(objectSpec);

        List<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(propertyFilterSpec);

        RetrieveOptions options = new RetrieveOptions();
        List<ObjectContent> listobcont =
                vimPort.retrievePropertiesEx(propCollectorRef, listpfs, options).getObjects();

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
                if (vmnm != null && vmnm.equals(vmUUID)) {
                    retVal = mr;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * @return TraversalSpec specification to get to the VirtualMachine managed
     *         object.
     */
    public TraversalSpec getVMTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the VirtualMachines.
        // Build the traversal specs bottoms up

        //Traversal to get to the VM in a VApp
        TraversalSpec vAppToVM = new TraversalSpecBuilder()
                .name("vAppToVM")
                .type("VirtualApp")
                .path("vm");

        //Traversal spec for VApp to VApp
        TraversalSpec vAppToVApp = new TraversalSpecBuilder()
                .name("vAppToVApp")
                .type("VirtualApp")
                .path("resourcePool")
                .selectSet(
                        //SelectionSpec for both VApp to VApp and VApp to VM
                        new SelectionSpecBuilder().name("vAppToVApp"),
                        new SelectionSpecBuilder().name("vAppToVM")
                );


        //This SelectionSpec is used for recursion for Folder recursion
        SelectionSpec visitFolders = new SelectionSpecBuilder().name("VisitFolders");

        // Traversal to get to the vmFolder from DataCenter
        TraversalSpec dataCenterToVMFolder = new TraversalSpecBuilder()
            .name("DataCenterToVMFolder")
            .type("Datacenter")
            .path("vmFolder")
            .skip(false)
            .selectSet(visitFolders);

        // TraversalSpec to get to the DataCenter from rootFolder
        return new TraversalSpecBuilder()
            .name("VisitFolders")
            .type("Folder")
            .path("childEntity")
            .skip(false)
            .selectSet(
                visitFolders,
                dataCenterToVMFolder,
                vAppToVM,
                vAppToVApp
            );
    }

    /**
     * Method to retrieve properties of a {@link ManagedObjectReference}
     *
     * @param entityMor {@link ManagedObjectReference} of the entity
     * @param props     Array of properties to be looked up
     * @return Map of the property name and its corresponding value
     * @throws InvalidPropertyFaultMsg If a property does not exist
     * @throws RuntimeFaultFaultMsg
     */
    public Map<String, Object> entityProps(
            ManagedObjectReference entityMor, String[] props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {

        init();

        final HashMap<String, Object> retVal = new HashMap<String, Object>();

        // Create PropertyFilterSpec using the PropertySpec and ObjectPec
        PropertyFilterSpec[] propertyFilterSpecs = {
                new PropertyFilterSpecBuilder()
                .propSet(
                        // Create Property Spec
                        new PropertySpecBuilder()
                            .all(Boolean.FALSE)
                            .type(entityMor.getType())
                            .pathSet(props)
                )
                .objectSet(
                        // Now create Object Spec
                        new ObjectSpecBuilder()
                                .obj(entityMor)
                )
        };

        List<ObjectContent> oCont =
                vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                        Arrays.asList(propertyFilterSpecs), new RetrieveOptions()).getObjects();

        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                List<DynamicProperty> dps = oc.getPropSet();
                for (DynamicProperty dp : dps) {
                    retVal.put(dp.getName(), dp.getVal());
                }
            }
        }
        return retVal;
    }

    /**
     * Method to retrieve properties of list of {@link ManagedObjectReference}
     *
     * @param entityMors List of {@link ManagedObjectReference} for which the properties
     *                   needs to be retrieved
     * @param props      Common properties that need to be retrieved for all the
     *                   {@link ManagedObjectReference} passed
     * @return Map of {@link ManagedObjectReference} and their corresponding name
     *         value pair of properties
     * @throws InvalidPropertyFaultMsg
     * @throws RuntimeFaultFaultMsg
     */
    public Map<ManagedObjectReference, Map<String, Object>> entityProps(
            List<ManagedObjectReference> entityMors, String[] props)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        init();

        Map<ManagedObjectReference, Map<String, Object>> retVal = new HashMap<ManagedObjectReference, Map<String, Object>>();
        // Create PropertyFilterSpec
        PropertyFilterSpecBuilder propertyFilterSpec = new PropertyFilterSpecBuilder();
        Map<String, String> typesCovered = new HashMap<String, String>();

        for (ManagedObjectReference mor : entityMors) {
            if (!typesCovered.containsKey(mor.getType())) {
                // Create & add new property Spec
                propertyFilterSpec.propSet(
                        new PropertySpecBuilder()
                                .all(Boolean.FALSE)
                                .type(mor.getType())
                                .pathSet(props)
                );
                typesCovered.put(mor.getType(), "");
            }
            // Now create & add Object Spec
            propertyFilterSpec.objectSet(
                    new ObjectSpecBuilder().obj(mor)
            );
        }
        List<PropertyFilterSpec> propertyFilterSpecs =
                new ArrayList<PropertyFilterSpec>();
        propertyFilterSpecs.add(propertyFilterSpec);

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(),
                        propertyFilterSpecs, new RetrieveOptions());

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        String token = populate(rslts,listobjcontent);
        while (token != null && !token.isEmpty()) {
            rslts =
                    vimPort.continueRetrievePropertiesEx(
                            serviceContent.getPropertyCollector(), token);

            token = populate(rslts,listobjcontent);
        }

        for (ObjectContent oc : listobjcontent) {
            List<DynamicProperty> dps = oc.getPropSet();
            Map<String, Object> propMap = new HashMap<String, Object>();
            if (dps != null) {
                for (DynamicProperty dp : dps) {
                    propMap.put(dp.getName(), dp.getVal());
                }
            }
            retVal.put(oc.getObj(), propMap);
        }
        return retVal;
    }


    public Map<String, ManagedObjectReference> inContainerByType(ManagedObjectReference container, String morefType) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return inContainerByType(container, morefType, new RetrieveOptions());
    }

    public Map<String,ManagedObjectReference> inFolderByType(ManagedObjectReference folder, String morefType) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        return inFolderByType(folder,morefType, new RetrieveOptions());
    }

    public Map<ManagedObjectReference,Map<String,Object>> inContainerByType(ManagedObjectReference container, String morefType, String[] strings) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return inContainerByType(container,morefType,strings,new RetrieveOptions());
    }

    public ManagedObjectReference getParent(ManagedObjectReference child, String type){
        ManagedObjectReference father = child;
        try {
            child = (ManagedObjectReference) entityProps(father, new String[]{"parent"}).get("parent");
            if (child != null){
                if (type.equalsIgnoreCase(child.getType())){
                    return child;
                }else {
                    return getParent(child,type);
                }
            }else {
                System.out.println("get the root folder is " + father.getValue());
                return father;
            }
        } catch (InvalidPropertyFaultMsg invalidPropertyFaultMsg) {
            invalidPropertyFaultMsg.printStackTrace();
        } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
            runtimeFaultFaultMsg.printStackTrace();
        }
        return null;
    }
}
