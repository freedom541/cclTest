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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * AcquireSessionInfo
 *
 * This sample will acquire a session with VC or ESX
 * and print a cim service ticket and related
 * session information to a file
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 * host        [required] : Name of the host
 * info        [optional] : Type of info required
 *                          only [cimticket] for now
 * file        [optional] : Full path of the file to save data to
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.Browser --url [webserviceurl]
 * --username [username] --password [password]
 * --host [hostname] --info [password] --file [path_to_file]
 * </pre>
 */
@Sample(
        name = "acquire-session-info",
        description = "This sample will acquire a session with VC or ESX " +
                "and print a cim service ticket and related session information to a file"
)
public class AcquireSessionInfo extends ConnectedVimServiceBase {
    private ManagedObjectReference propCollectorRef;

    private String hostname;
    private String info;
    private String filename;

    @Option(name = "host", description = "name of host")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Option(name = "info", required = false, description = "Type of info required [cimticket]")
    public void setInfo(String info) {
        this.info = info;
    }

    @Option(name = "file", required = false, description = "Full path of the file to save data to")
    public void setFilename(String filename) {
        this.filename = filename;
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
     * Uses the new RetrievePropertiesEx method to emulate the now deprecated
     * RetrieveProperties method
     *
     * @param listpfs
     * @return list of object content
     * @throws Exception
     */
    List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

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
     * Retrieves the MOREF of the host.
     *
     * @param hostName
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
        return retVal;
    }

    String stringToWrite(HostServiceTicket serviceTicket) {
        String sslThumbprint = "undefined";
        String host = "undefined";
        String port = "undefined";
        String service = serviceTicket.getService();
        String serviceVersion = serviceTicket.getServiceVersion();
        String serviceSessionId = serviceTicket.getSessionId();

        if (serviceTicket.getSslThumbprint() != null) {
            sslThumbprint = serviceTicket.getSslThumbprint();

        }
        if (serviceTicket.getHost() != null) {
            host = serviceTicket.getHost();

        }
        if (serviceTicket.getPort() != null) {
            port = Integer.toString(serviceTicket.getPort());

        }
        StringBuilder datatowrite = new StringBuilder("");
        datatowrite.append("CIM Host Service Ticket Information\n");
        datatowrite.append("Service        : ");
        datatowrite.append(service);
        datatowrite.append("\n");
        datatowrite.append("Service Version: ");
        datatowrite.append(serviceVersion);
        datatowrite.append("\n");
        datatowrite.append("Session Id     : ");
        datatowrite.append(serviceSessionId);
        datatowrite.append("\n");
        datatowrite.append("SSL Thumbprint : ");
        datatowrite.append(sslThumbprint);
        datatowrite.append("\n");
        datatowrite.append("Host           : ");
        datatowrite.append(host);
        datatowrite.append("\n");
        datatowrite.append("Port           : ");
        datatowrite.append(port);
        datatowrite.append("\n");
        System.out.println("CIM Host Service Ticket Information\n");
        System.out.println("Service           : " + service);
        System.out.println("Service Version   : " + serviceVersion);
        System.out.println("Session Id        : " + serviceSessionId);
        System.out.println("SSL Thumbprint    : " + sslThumbprint);
        System.out.println("Host              : " + host);
        System.out.println("Port              : " + port);
        return datatowrite.toString();
    }

    void acquireSessionInfo() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, IOException {
        ManagedObjectReference hostmor = getHostByHostName(hostname);

        if (hostmor == null) {
            String msg = "Failure: Host [ " + hostname + "] not found";
            throw new HostFailure(msg);
        }

        if ((info == null) || (info.equalsIgnoreCase("cimticket"))) {

            HostServiceTicket serviceTicket =
                    vimPort.acquireCimServicesTicket(hostmor);

            if (serviceTicket != null) {
                String datatoWrite = stringToWrite(serviceTicket);
                writeToFile(datatoWrite, filename);
            }
        } else {
            System.out.println("Support for " + info + " not implemented.");
        }
    }

    void writeToFile(String data, String fileName)
            throws IOException {
        fileName = fileName == null ? "cimTicketInfo.txt" : fileName;
        File cimFile = new File(fileName);
        FileOutputStream fop = new FileOutputStream(cimFile);
        if (cimFile.exists()) {
            String str = data;
            fop.write(str.getBytes());
            fop.flush();
            fop.close();
            System.out.println("Saved session information at "
                    + cimFile.getAbsolutePath());
        }
    }

    @Action
    public void run() throws RuntimeFaultFaultMsg, IOException, InvalidPropertyFaultMsg {
        propCollectorRef = serviceContent.getPropertyCollector();
        acquireSessionInfo();
    }

    private class HostFailure extends RuntimeException {
		private static final long serialVersionUID = 1L;
        public HostFailure(String msg) {
            super(msg);
        }
    }
}
