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
 * TaskList
 *
 * This sample prints out a list of tasks if any are running
 * The sample display the tasks in the following format:
 * Operation:
 * Name:
 * Type:
 * State:
 * Error:
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.TaskList --url [webserviceurl]
 * --username [username] --password [password]
 * </pre>
 */

@Sample(name = "task-list", description = "This sample prints out a list of tasks if any are running")
public class TaskList extends ConnectedVimServiceBase {

    private ManagedObjectReference propCollectorRef;

    List<PropertyFilterSpec> createPFSForRecentTasks(
            ManagedObjectReference taskManagerRef) {
        PropertySpec pSpec = new PropertySpec();
        pSpec.setAll(Boolean.FALSE);
        pSpec.setType("Task");
        List<String> listofprop = new ArrayList<String>();
        listofprop.add("info.entity");
        listofprop.add("info.entityName");
        listofprop.add("info.name");
        listofprop.add("info.state");
        listofprop.add("info.cancelled");
        listofprop.add("info.error");
        pSpec.getPathSet().addAll(listofprop);

        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(taskManagerRef);
        oSpec.setSkip(Boolean.FALSE);

        TraversalSpec tSpec = new TraversalSpec();
        tSpec.setType("TaskManager");
        tSpec.setPath("recentTask");
        tSpec.setSkip(Boolean.FALSE);

        oSpec.getSelectSet().add(tSpec);

        PropertyFilterSpec pfSpec = new PropertyFilterSpec();
        pfSpec.getPropSet().add(pSpec);
        pfSpec.getObjectSet().add(oSpec);

        List<PropertyFilterSpec> result = new ArrayList<PropertyFilterSpec>();
        result.add(pfSpec);
        return result;
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

    void displayTasks(List<ObjectContent> oContents) {
        for (int oci = 0; oci < oContents.size(); ++oci) {
            System.out.println("Task");
            List<DynamicProperty> dplist = oContents.get(oci).getPropSet();

            if (dplist != null) {
                String op = "", name = "", type = "", state = "", error = "";
                for (int dpi = 0; dpi < dplist.size(); ++dpi) {
                    DynamicProperty dp = dplist.get(dpi);
                    if ("info.entity".equals(dp.getName())) {
                        type = ((ManagedObjectReference) dp.getVal()).getType();
                    } else if ("info.entityName".equals(dp.getName())) {
                        name = ((String) dp.getVal());
                    } else if ("info.name".equals(dp.getName())) {
                        op = ((String) dp.getVal());
                    } else if ("info.state".equals(dp.getName())) {
                        TaskInfoState tis = (TaskInfoState) dp.getVal();
                        if (TaskInfoState.ERROR.equals(tis)) {
                            state = "-Error";
                        } else if (TaskInfoState.QUEUED.equals(tis)) {
                            state = "-Queued";
                        } else if (TaskInfoState.RUNNING.equals(tis)) {
                            state = "-Running";
                        } else if (TaskInfoState.SUCCESS.equals(tis)) {
                            state = "-Success";
                        }
                    } else if ("info.cancelled".equals(dp.getName())) {
                        Boolean b = (Boolean) dp.getVal();
                        if (b != null && b.booleanValue()) {
                            state += "-Cancelled";
                        }
                    } else if ("info.error".equals(dp.getName())) {
                        LocalizedMethodFault mf = (LocalizedMethodFault) dp.getVal();
                        if (mf != null) {
                            error = mf.getLocalizedMessage();
                        }
                    } else {
                        op =
                                "Got unexpected property: " + dp.getName() + " Value: "
                                        + dp.getVal().toString();
                    }
                }
                System.out.println("Operation " + op);
                System.out.println("Name " + name);
                System.out.println("Type " + type);
                System.out.println("State " + state);
                System.out.println("Error " + error);
                System.out.println("======================");
            }
        }
        if (oContents.size() == 0) {
            System.out.println("Currently no task running");
        }
    }

    @Action
    public List<ObjectContent> list() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        ManagedObjectReference taskManagerRef = serviceContent.getTaskManager();
        List<PropertyFilterSpec> listpfs = createPFSForRecentTasks(taskManagerRef);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            displayTasks(listobjcont);
        } else {
            System.out.println("Currently no task running");
        }
        return listobjcont;
    }
}
