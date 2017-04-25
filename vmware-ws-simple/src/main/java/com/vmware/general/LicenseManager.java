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
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * LicenseManager
 *
 * Demonstrates uses of the Licensing API using License Manager
 * Reference.
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 * action      [required] : action to be performed
 *                          [browse|setserver|setedition|featureinfo]
 * feature     [optional] : Licensed feature e.g. vMotion
 * licensekey  [optional] : License key for KL servers
 *
 * <b>Command Line:</b>
 * Display all license information
 * run.bat com.vmware.general.LicenseManager --url [webserviceurl]
 * --username [username] --password [password] --action[browse]
 *
 * Retrieve the feature information
 * run.bat com.vmware.general.LicenseManager --url [webserviceurl]
 * --username [username] --password [password] --action[featureinfo] --feature [drs]
 *
 * run.bat com.vmware.general.LicenseManager --url [webserviceurl]
 * --username [username] --password [password] --action[setserver] --licensekey [key]
 * </pre>
 */
@Sample(name = "license-manager", description = "Demonstrates uses of the Licensing API")
public class LicenseManager extends ConnectedVimServiceBase {

    /* Start Sample functional code */

    String action = null;
    String feature = null;
    String licenseKey = null;

    @Option(name = "action", description = "action to be performed: [browse|setserver|setedition|featureinfo]")
    public void setAction(String act) {
        this.action = act;
    }

    @Option(name = "feature", required = false, description = "licensed feature")
    public void setFeature(String feature) {
        this.feature = feature;
    }

    @Option(name = "licensekey", required = false, description = "License key for KL servers")
    public void setLicenseKey(String key) {
        this.licenseKey = key;
    }

    private ManagedObjectReference licManagerRef = null;
    private ManagedObjectReference licenseAssignmentManagerRef = null;
    private ArrayOfLicenseFeatureInfo featureInfo;
    private ManagedObjectReference propCollector;

    public void init() {
        propCollector = serviceContent.getPropertyCollector();
    }

    public void initLicManagerRef() {
        if (serviceContent != null) {
            licManagerRef = serviceContent.getLicenseManager();
        }
    }

    public void initLicAssignmentManagerRef() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ArrayList<PropertyFilterSpec> listpfspec =
                new ArrayList<PropertyFilterSpec>();
        listpfspec.add(createEventFilterSpec("licenseAssignmentManager"));
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfspec);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        licenseAssignmentManagerRef =
                                (ManagedObjectReference) dp.getVal();
                    }
                }
            }
        }
    }

    public void initfeatureInfoRef() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ArrayList<PropertyFilterSpec> listpfspec =
                new ArrayList<PropertyFilterSpec>();
        listpfspec.add(createEventFilterSpec("featureInfo"));
        List<ObjectContent> listobjcont =
                retrievePropertiesAllObjects(listpfspec);
        if (listobjcont != null) {
            for (ObjectContent oc : listobjcont) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        featureInfo = (ArrayOfLicenseFeatureInfo) dp.getVal();
                    }
                }
            }
        }
    }

    public void useLicenseManager() throws RuntimeFaultFaultMsg, LicenseEntityNotFoundFaultMsg {
        if (action.equalsIgnoreCase("browse")) {
            System.out.println("Display the license usage. "
                    + "It gives details of license features " + "like license key "
                    + " edition key and entity id.");
            displayLicenseUsage();
        } else if (action.equalsIgnoreCase("setserver")) {
            System.out.println("Adding the license key.");
            setLicenseServer();
        } else if (action.equalsIgnoreCase("featureinfo")) {
            if (feature != null) {
                displayFeatureInfo();
            } else {
                throw new IllegalArgumentException("Expected --feature argument.");
            }
        } else {
            System.out.println("Invalid Action ");
            System.out.println("Valid Actions [browse|setserver|featureinfo]");
        }
    }

    public PropertyFilterSpec createEventFilterSpec(String property) {
        PropertySpec propSpec = new PropertySpec();
        propSpec.setAll(new Boolean(false));
        propSpec.getPathSet().add(property);
        propSpec.setType(licManagerRef.getType());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(licManagerRef);
        objSpec.setSkip(new Boolean(false));

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(propSpec);
        spec.getObjectSet().add(objSpec);
        return spec;
    }

    /**
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method.
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    public List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
                vimPort.retrievePropertiesEx(propCollector, listpfs,
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
            rslts = vimPort.continueRetrievePropertiesEx(propCollector, token);
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

    public void displayLicenseUsage() throws RuntimeFaultFaultMsg {
        List<LicenseAssignmentManagerLicenseAssignment> avail =
                vimPort.queryAssignedLicenses(licenseAssignmentManagerRef, null);
        print(avail);
    }

    public void setLicenseServer() throws RuntimeFaultFaultMsg, LicenseEntityNotFoundFaultMsg {
        boolean flag = true;
        if (licenseKey == null) {
            System.out
                    .println("Error:: For KL servers licensekey is a mandatory option");
            flag = false;
        }
        if (flag) {
            String apitype = serviceContent.getAbout().getApiType();
            if (apitype.equalsIgnoreCase("VirtualCenter")) {
                String entity = serviceContent.getAbout().getInstanceUuid();
                vimPort.updateAssignedLicense(licenseAssignmentManagerRef,
                        entity, licenseKey, null);
                System.out.println("License key set for VC server");
            } else if (apitype.equalsIgnoreCase("HostAgent")) {
                vimPort.decodeLicense(licManagerRef, licenseKey);
                vimPort.updateLicense(licManagerRef, licenseKey, null);
                System.out.println("License key set for ESX server");
            }
        }
    }

    public void displayFeatureInfo() {
        List<LicenseFeatureInfo> feaTure = new ArrayList<LicenseFeatureInfo>();
        feaTure = featureInfo.getLicenseFeatureInfo();
        if (feaTure != null) {
            for (LicenseFeatureInfo featureinfo : feaTure) {
                if (featureinfo.getKey().equalsIgnoreCase(feature)) {
                    System.out.println("Name       " + featureinfo.getFeatureName());
                    System.out.println("Unique Key " + featureinfo.getKey());
                    System.out.println("State      " + featureinfo.getState());
                    System.out.println("Cost Unit  " + featureinfo.getCostUnit());
                }
            }
        } else if (feaTure == null) {
            System.out.println("Feature Not Available");
        }
    }

    public void print(
            List<LicenseAssignmentManagerLicenseAssignment> licAssignment) {
        if (licAssignment != null) {
            for (LicenseAssignmentManagerLicenseAssignment la : licAssignment) {
                String entityId = la.getEntityId();
                String editionKey = la.getAssignedLicense().getEditionKey();
                String licensekey = la.getAssignedLicense().getLicenseKey();
                String name = la.getAssignedLicense().getName();
                System.out.println("\nName of the license: " + name
                        + "\n License Key:  " + licensekey + "\n Edition Key: "
                        + editionKey + "\n EntityID: " + entityId + "\n\n");
            }
        }
    }

    @Action
    public void action() throws RuntimeFaultFaultMsg, LicenseEntityNotFoundFaultMsg, InvalidPropertyFaultMsg {
        init();
        initLicManagerRef();
        initLicAssignmentManagerRef();
        initfeatureInfoRef();
        useLicenseManager();
    }
}
