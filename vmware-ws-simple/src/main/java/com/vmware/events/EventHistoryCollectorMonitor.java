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

package com.vmware.events;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * EventHistoryCollectorMonitor
 *
 * This sample demonstrates how to create and monitor an EventHistoryCollector
 * This sample uses the latestPage property of the EventHistoryCollector
 * to filter the Events
 *
 * <b>Parameters:</b>
 * url            [required] : url of the web service
 * username       [required] : username for the authentication
 * password       [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.vm.EventHistoryCollectorMonitor
 * --url [webserviceurl] --username [username] --password [password]
 * </pre>
 */

@Sample(
        name = "event-history-collector-monitor",
        description = "This sample demonstrates how to create and monitor an EventHistoryCollector " +
                "This sample uses the latestPage property of the EventHistoryCollector " +
                "to filter the Events"
)
public class EventHistoryCollectorMonitor extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollector;
    private ManagedObjectReference eventManager;
    private ManagedObjectReference eventHistoryCollector;

    void initEventManagerRef() {
        if (serviceContent != null) {
            eventManager = serviceContent.getEventManager();
        }
    }

    void createEventHistoryCollector() throws RuntimeFaultFaultMsg, InvalidStateFaultMsg {
        EventFilterSpec eventFilter = new EventFilterSpec();
        eventHistoryCollector =
                vimPort.createCollectorForEvents(eventManager, eventFilter);
    }

    PropertyFilterSpec createEventFilterSpec() {
        PropertySpec propSpec = new PropertySpec();
        propSpec.setAll(new Boolean(false));
        propSpec.getPathSet().add("latestPage");
        propSpec.setType(eventHistoryCollector.getType());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(eventHistoryCollector);
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
    List<ObjectContent> retrievePropertiesAllObjects(
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

    void monitorEvents(PropertyFilterSpec spec) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
        ArrayList<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>();
        listpfs.add(spec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
        if (listobjcont != null) {
            ObjectContent oc = listobjcont.get(0);
            ArrayOfEvent arrayEvents =
                    (ArrayOfEvent) (oc.getPropSet().get(0)).getVal();

            ArrayList<Event> eventList = (ArrayList<Event>) arrayEvents.getEvent();
            System.out.println("Events In the latestPage are: ");
            for (int i = 0; i < eventList.size(); i++) {
                Event anEvent = eventList.get(i);
                System.out.println("Event: " + anEvent.getClass().getName());
            }
        } else {
            System.out.println("No Events retrieved!");
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, InvalidStateFaultMsg, InvalidPropertyFaultMsg {
        propCollector = serviceContent.getPropertyCollector();
        initEventManagerRef();
        createEventHistoryCollector();
        PropertyFilterSpec eventFilterSpec = createEventFilterSpec();
        monitorEvents(eventFilterSpec);
    }

}
