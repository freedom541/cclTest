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

import com.vmware.common.annotations.*;
import com.vmware.connection.ConnectedVimServiceBase;


/**
 * <pre>
 * Connect
 *
 * This sample simply connects to an ESX server or to vCenter server
 *
 * <b>Parameters:</b>
 * url         [required] : url of the web service
 * username    [required] : username for the authentication
 * password    [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.Connect --url [webserviceurl]
 * --username [username] --password [password]
 * </pre>
 * @see com.vmware.connection.ConnectedVimServiceBase
 * @see com.vmware.connection.BasicConnection
 * @see com.vmware.connection.SsoConnection
 */
@Sample(
        name = "connect",
        description = "This sample connects to an ESX server or to vCenter server."
)
public class Connect extends ConnectedVimServiceBase {
    /**
     * For details on how to form different types of connections
     * to various kinds of vSphere web service agents, see the
     * classes under the package com.vmware.connection,
     * @see com.vmware.connection.BasicConnection
     * @see com.vmware.connection.SsoConnection
     */
    @Action
    public void action() {
           System.out.printf("connected to: %s", connection.getServiceContent().getAbout().getLicenseProductName());
    }

}