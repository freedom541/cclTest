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
import com.vmware.vim25.AboutInfo;

/**
 * Demonstrates how to use the AboutInfo object to find out more about the
 * vSphere WS API end-point you are currently connected to.
 * <p/>
 *
 */
@Sample(
        name = "info",
        description = "Prints information about the vSphere WS API end-point you are currently connected to. " +
                      "This sample can be used with an ESX, ESXi, or vCenter vSphere WS API end-point. " +
                      "Note: ESX and ESXi hosts are reported as HostAgent connections."
)
public class Info extends ConnectedVimServiceBase {
    @Action
    public void main() {
        String hostName = connection.getHost();
        AboutInfo aboutInfo = connection.connect().getServiceContent().getAbout();
        System.out.printf("You are connected to %s %n", hostName);
        System.out.printf("Host %s is running SDK version: %s %n", hostName, aboutInfo.getVersion());
        System.out.printf("Host %s is a %s %n", hostName, aboutInfo.getApiType());
    }
}
