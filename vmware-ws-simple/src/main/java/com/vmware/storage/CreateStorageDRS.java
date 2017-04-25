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

package com.vmware.storage;

import com.vmware.common.Main;
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
 * This sample demonstrates how to create Storage DRS
 * <b>Parameters:</b>
 * url                       [required]: url of the web service.
 * username                  [required]: username for the authentication
 * password                  [required]: password for the authentication
 * dcname                    [required]: DataCenter name.
 * sdrsname                  [required]: Name for the new storage pod.
 * behavior                  [optional]: Storage DRS behavior, true if automated. It is
 *                                       manual by default.
 * iolatencythreshold        [optional]: int: Storage DRS makes storage migration
 *                                       recommendations if I/O latency on one (or more)
 *                                       of the datastores is higher than the specified
 *                                       threshold. Range is 5-100 ms, default is 15ms
 * ioloadimbalancethreshold  [optional]: Storage DRS makes storage migration
 *                                       recommendations if I/O load imbalance
 *                                       level is higher than the specified threshold.
 *                                       Range is 1-100, default is 5
 * loadbalinterval           [optional]: Specify the interval that storage DRS runs to
 *                                       load balance among datastores within a storage
 *                                       pod. it is 480 by default.
 * minutilizationdiff        [optional]: Storage DRS considers making storage migration
 *                                       recommendations if the difference in space
 *                                       utilization between the source and  destination
 *                                       datastores is higher than the specified threshold.
 *                                       Range 1-50%, default is 5%
 * utilizationthreshold      [optional]: Storage DRS makes storage migration
 *                                       recommendations if space utilization on one
 *                                       (or more) of the datastores is higher than the
 *                                       specified threshold. Range 50-100%, default is 80%
 * datastore                 [optional]: Name of the datastore to be added in StoragePod.
 * <b>Sample usage:</b>
 *  run.bat com.vmware.storage.CreateStorageDRS --url [URLString] --username [User]
 *  --password [Password] --dcname [dcname] --sdrsname [sdrsname] --behavior [behavior]
 *  --iolatencythreshold [iolatencythreshold] --ioloadimbalancethreshold
 *  [ioloadimbalancethreshold] --loadbalinterval [loadbalinterval] --minutilizationdiff
 *  [minutilizationdiff] --utilizationthreshold [utilizationthreshold] --datastore [datastore]
 * </pre>
 */
@Sample(name = "create-storage-drs",
        description = "This sample demonstrates how to create Storage DRS"
)
public class CreateStorageDRS extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;

    String dcName = null;
    String sdrsname = null;
    Boolean behavior = Boolean.FALSE;
    String ioLatencyThreshold = null;
    String ioLoadImbalanceThreshold = null;
    String loadBalanceInterval = null;
    String minSpaceUtilizationDifference = null;
    String spaceUtilizationThreshold = null;
    String dsname = null;

    @Option(name = "dcname", description = "data center name")
    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    @Option(name = "sdrsname", description = "Name for the new storage pod.")
    public void setSdrsname(String sdrsname) {
        this.sdrsname = sdrsname;
    }

    @Option(name = "behavior", required = false,
            description = "Storage DRS behavior: 'true' if automated. It is manual by default.")
    public void setBehavior(String behavior) {
        this.behavior = new Boolean(behavior);
    }

    @Option(name = "iolatencythreshold", required = false,
            description = "int: Storage DRS makes storage migration\n" +
                    "recommendations if I/O latency on one (or more)\n" +
                    "of the datastores is higher than the specified\n" +
                    "threshold. Range is 5-100 ms, default is 15ms\n")
    public void setIoLatencyThreshold(String ioLatencyThreshold) {
        this.ioLatencyThreshold = ioLatencyThreshold;
    }

    @Option(name = "ioloadimbalancethreshold", required = false,
            description = "\n\tStorage DRS makes storage migration\n" +
                    "recommendations if I/O load imbalance\n" +
                    "level is higher than the specified threshold.\n" +
                    "Range is 1-100, default is 5\n")
    public void setIoLoadImbalanceThreshold(String ioLoadImbalanceThreshold) {
        this.ioLoadImbalanceThreshold = ioLoadImbalanceThreshold;
    }

    @Option(name = "loadbalinterval", required = false,
            description = "Specify the interval that storage DRS runs to\n" +
                    "load balance among datastores within a storage\n" +
                    "pod. it is 480 by default.")
    public void setLoadBalanceInterval(String loadBalanceInterval) {
        this.loadBalanceInterval = loadBalanceInterval;
    }

    @Option(name = "minutilizationdiff", required = false,
            description = "Storage DRS considers making storage migration\n" +
                    "recommendations if the difference in space\n" +
                    "utilization between the source and  destination\n" +
                    "datastores is higher than the specified threshold.\n" +
                    "Range 1-50%, default is 5%")
    public void setMinSpaceUtilizationDifference(String minSpaceUtilizationDifference) {
        this.minSpaceUtilizationDifference = minSpaceUtilizationDifference;
    }

    @Option(name = "utilizationthreshold", required = false,
            description = "Storage DRS makes storage migration\n" +
                    "recommendations if space utilization on one\n" +
                    "(or more) of the datastores is higher than the\n" +
                    "specified threshold. Range 50-100%, default is 80%")
    public void setSpaceUtilizationThreshold(String spaceUtilizationThreshold) {
        this.spaceUtilizationThreshold = spaceUtilizationThreshold;
    }

    @Option(name = "datastore", required = false, description = "Name of the datastore to be added in StoragePod.")
    public void setDsname(String dsname) {
        this.dsname = dsname;
    }

    /**
     * Validates the options supplied to the command and throws errors if a good combination has not been
     * entered for the sample to operate.
     */
    void validate() {
        if ((sdrsname == null) || (dcName == null)) {
            throw new IllegalArgumentException(
                    "Expected valid -dcname, --sdrsname" + " arguments.");
        }
        if (ioLatencyThreshold != null) {
            if (Integer.parseInt(ioLatencyThreshold) < 5
                    || Integer.parseInt(ioLatencyThreshold) > 50) {
                throw new IllegalArgumentException(
                        "Expected valid --iolatencythreshold argument. Range is 5-50 ms.");
            }
        }
        if (ioLoadImbalanceThreshold != null) {
            if (Integer.parseInt(ioLoadImbalanceThreshold) < 1
                    || Integer.parseInt(ioLoadImbalanceThreshold) > 100) {
                throw new IllegalArgumentException(
                        "Expected valid --ioloadimbalancethreshold argument. Range is 1-100.");
            }
        }
        if (minSpaceUtilizationDifference != null) {
            if (Integer.parseInt(minSpaceUtilizationDifference) < 1
                    || Integer.parseInt(minSpaceUtilizationDifference) > 50) {
                throw new IllegalArgumentException(
                        "Expected valid --minutilizationdiff argument. Range is 1-50%.");
            }
        }
        if (spaceUtilizationThreshold != null) {
            if (Integer.parseInt(spaceUtilizationThreshold) < 50
                    || Integer.parseInt(spaceUtilizationThreshold) > 100) {
                throw new IllegalArgumentException(
                        "Expected valid --utilizationthreshold argument. Range is 50-100%.");
            }
        }
    }


    /**
     * Uses the new RetrievePropertiesEx method to emulate the now Depreciated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) {
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        try {
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
        } catch (Exception e) {
            System.out.println(" : Failed Getting Contents");
            e.printStackTrace();
        }

        return listobjcontent;
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
            ManagedObjectReference mobj, String[] properties) {
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
                         Class[] parameterTypes) {
        boolean exists = false;
        try {
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            if (method != null) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    /**
     * Retrieve a single object.
     *
     * @param mor Managed Object Reference to get contents for
     * @return retrieved object
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws Main.SampleIllegalArgumentException
     *
     */
    Object getDatastoreFolder(ManagedObjectReference mor)
            throws SecurityException, NoSuchMethodException,
            Main.SampleIllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        ObjectContent[] objContent =
                getObjectProperties(mor, new String[]{"datastoreFolder"});

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
                    * If object is ArrayOfXXX object, then get the xxx[] by
                    * invoking getXXX() on the object.
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
     * @return An array of SelectionSpec covering Datacenter to DatastoreFolder.
     */
    SelectionSpec[] buildTraversal() {

        // For Folder -> Folder recursion
        SelectionSpec sspecvfolders = new SelectionSpec();
        sspecvfolders.setName("VisitFolders");

        TraversalSpec dcToDf = new TraversalSpec();
        dcToDf.setType("Datacenter");
        dcToDf.setSkip(Boolean.FALSE);
        dcToDf.setPath("datastoreFolder");
        dcToDf.setName("dcToDf");
        dcToDf.getSelectSet().add(sspecvfolders);

        // DC -> DS
        TraversalSpec dcToDs = new TraversalSpec();
        dcToDs.setType("Datacenter");
        dcToDs.setPath("datastore");
        dcToDs.setName("dcToDs");
        dcToDs.setSkip(Boolean.FALSE);

        TraversalSpec visitFolders = new TraversalSpec();
        visitFolders.setType("Folder");
        visitFolders.setPath("childEntity");
        visitFolders.setSkip(Boolean.FALSE);
        visitFolders.setName("VisitFolders");

        List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
        sspecarrvf.add(dcToDs);
        sspecarrvf.add(dcToDf);
        sspecarrvf.add(sspecvfolders);

        visitFolders.getSelectSet().addAll(sspecarrvf);
        return new SelectionSpec[]{visitFolders};
    }

    /**
     * Getting the MOREF of the entity.
     */
    ManagedObjectReference getEntityByName(String entityName,
                                           String entityType) {
        ManagedObjectReference retVal = null;

        try {
            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.setType(entityType);
            propertySpec.getPathSet().add("name");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(rootRef);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().addAll(Arrays.asList(buildTraversal()));

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
                    if (oc.getPropSet().get(0).getVal().equals(entityName)) {
                        retVal = oc.getObj();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * @param behavior                      Storage DRS behavior, true if automated.
     * @param ioLatencyThreshold            Storage DRS makes storage migration recommendations if I/O
     *                                      latency on one (or more) of the datastores is higher than the
     *                                      specified threshold.
     * @param ioLoadImbalanceThreshold      Storage DRS makes storage migration recommendations if I/O load
     *                                      imbalance level is higher than the specified threshold.
     * @param loadBalanceInterval           Specify the interval that storage DRS runs to load balance among
     *                                      datastores within a storage pod.
     * @param minSpaceUtilizationDifference Storage DRS considers making storage migration recommendations
     *                                      if the difference in space utilization between the source and
     *                                      destination datastores is higher than the specified threshold.
     * @param spaceUtilizationThreshold     Storage DRS makes storage migration recommendations if space
     *                                      utilization on one (or more) of the datastores is higher than
     *                                      the specified threshold.
     * @return StorageDrsConfigSpec object.
     */
    StorageDrsConfigSpec getStorageDrsConfigSpec( boolean behavior,
                                                 String ioLatencyThreshold, String ioLoadImbalanceThreshold,
                                                 String loadBalanceInterval, String minSpaceUtilizationDifference,
                                                 String spaceUtilizationThreshold) {
        StorageDrsConfigSpec sdrsConfigSpec = new StorageDrsConfigSpec();
        StorageDrsPodConfigSpec podConfigSpec = new StorageDrsPodConfigSpec();
        podConfigSpec.setDefaultIntraVmAffinity(true);
        if (behavior) {
            podConfigSpec.setDefaultVmBehavior("automated");
        } else {
            podConfigSpec.setDefaultVmBehavior("manual");
        }
        podConfigSpec.setEnabled(true); // "True" as storage DRS should be enabled by default
        StorageDrsIoLoadBalanceConfig sdrsIoLoadBalanceConfig =
                new StorageDrsIoLoadBalanceConfig();
        if (ioLatencyThreshold != null) {
            sdrsIoLoadBalanceConfig.setIoLatencyThreshold(Integer
                    .parseInt(ioLatencyThreshold));
        } else {
            sdrsIoLoadBalanceConfig.setIoLatencyThreshold(15);
        }
        if (ioLoadImbalanceThreshold != null) {
            sdrsIoLoadBalanceConfig.setIoLoadImbalanceThreshold(Integer
                    .parseInt(ioLoadImbalanceThreshold));
        } else {
            sdrsIoLoadBalanceConfig.setIoLoadImbalanceThreshold(5);
        }
        podConfigSpec.setIoLoadBalanceConfig(sdrsIoLoadBalanceConfig);

        podConfigSpec.setIoLoadBalanceEnabled(true);
        if (loadBalanceInterval != null) {
            podConfigSpec.setLoadBalanceInterval(Integer
                    .parseInt(loadBalanceInterval));
        } else {
            podConfigSpec.setLoadBalanceInterval(480);
        }
        StorageDrsSpaceLoadBalanceConfig sdrsSpaceLoadBalanceConfig =
                new StorageDrsSpaceLoadBalanceConfig();
        if (minSpaceUtilizationDifference != null) {
            sdrsSpaceLoadBalanceConfig.setMinSpaceUtilizationDifference(Integer
                    .parseInt(minSpaceUtilizationDifference));
        } else {
            sdrsSpaceLoadBalanceConfig.setMinSpaceUtilizationDifference(5);
        }
        if (spaceUtilizationThreshold != null) {
            sdrsSpaceLoadBalanceConfig.setSpaceUtilizationThreshold(Integer
                    .parseInt(spaceUtilizationThreshold));
        } else {
            sdrsSpaceLoadBalanceConfig.setSpaceUtilizationThreshold(80);
        }
        podConfigSpec.setSpaceLoadBalanceConfig(sdrsSpaceLoadBalanceConfig);
        sdrsConfigSpec.setPodConfigSpec(podConfigSpec);
        return sdrsConfigSpec;
    }

    /**
     * For creating customize StorageDRS.
     *
     * @param dcName                        datacenter name
     * @param behavior                      Storage DRS behavior, true if automated.
     * @param ioLatencyThreshold            Storage DRS makes storage migration recommendations if I/O
     *                                      latency on one (or more) of the datastores is higher than the
     *                                      specified threshold.
     * @param ioLoadImbalanceThreshold      Storage DRS makes storage migration recommendations if I/O load
     *                                      imbalance level is higher than the specified threshold.
     * @param loadBalanceInterval           Specify the interval that storage DRS runs to load balance among
     *                                      datastores within a storage pod.
     * @param minSpaceUtilizationDifference Storage DRS considers making storage migration recommendations
     *                                      if the difference in space utilization between the source and
     *                                      destination datastores is higher than the specified threshold.
     * @param spaceUtilizationThreshold     Storage DRS makes storage migration recommendations if space
     *                                      utilization on one (or more) of the datastores is higher than
     *                                      the specified threshold.
     * @throws Exception
     */
    void createSDRS(String dcName, String name, boolean behavior,
                    String ioLatencyThreshold, String ioLoadImbalanceThreshold,
                    String loadBalanceInterval, String minSpaceUtilizationDifference,
                    String spaceUtilizationThreshold, String dsname) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvalidNameFaultMsg, InvalidPropertyFaultMsg, InvalidCollectorVersionFaultMsg, InvalidFolderFaultMsg, InvalidStateFaultMsg {
        ManagedObjectReference storagePod = new ManagedObjectReference();
        ManagedObjectReference storageResourceManager =
                serviceContent.getStorageResourceManager();
        ManagedObjectReference dcmor = getEntityByName(dcName, "Datacenter");
        if (dcmor != null) {
            ManagedObjectReference datastoreFolder =
                    (ManagedObjectReference) getDatastoreFolder(dcmor);
            storagePod = vimPort.createStoragePod(datastoreFolder, name);
            System.out.printf("Success: Creating storagePod.");
            StorageDrsConfigSpec sdrsConfigSpec =
                    getStorageDrsConfigSpec(behavior, ioLatencyThreshold,
                            ioLoadImbalanceThreshold, loadBalanceInterval,
                            minSpaceUtilizationDifference, spaceUtilizationThreshold);
            ManagedObjectReference taskmor =
                    vimPort.configureStorageDrsForPodTask(storageResourceManager,
                            storagePod, sdrsConfigSpec, true);
            if (getTaskResultAfterDone(taskmor)) {
                System.out.printf("\nSuccess: Configuring storagePod.");
            } else {
                throw new RuntimeException("Failure: Configuring storagePod.");
            }
            if (dsname != null) {
                ManagedObjectReference dsMoref =
                        getEntityByName(dsname, "Datastore");
                if (dsMoref != null) {
                    List<ManagedObjectReference> dsList =
                            new ArrayList<ManagedObjectReference>();
                    dsList.add(dsMoref);
                    ManagedObjectReference task =
                            vimPort.moveIntoFolderTask(storagePod, dsList);
                    if (getTaskResultAfterDone(task)) {
                        System.out
                                .printf("\nSuccess: Adding datastore to storagePod.");
                    } else {
                        throw new RuntimeException(
                                "Failure: Adding datastore to storagePod.");
                    }
                } else {
                    System.out.println("Datastore " + dsname + " Not Found");
                    return;
                }
            }
        } else {
            System.out.println("Datacenter " + dcName + " Not Found");
            return;
        }
    }

    @Action
    public void run() throws DuplicateNameFaultMsg, RuntimeFaultFaultMsg, InvocationTargetException, InvalidFolderFaultMsg, InvalidPropertyFaultMsg, NoSuchMethodException, InvalidStateFaultMsg, InvalidCollectorVersionFaultMsg, InvalidNameFaultMsg, IllegalAccessException {
        validate();
        propCollectorRef = serviceContent.getPropertyCollector();
        createSDRS(dcName, sdrsname, behavior, ioLatencyThreshold,
                ioLoadImbalanceThreshold, loadBalanceInterval,
                minSpaceUtilizationDifference, spaceUtilizationThreshold, dsname);
    }
}
