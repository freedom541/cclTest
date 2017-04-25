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
import com.vmware.vim25.RuntimeFaultFaultMsg;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;

/**
 * <pre>
 * GetCurrentTime
 *
 * This sample gets the current time of the vSphere Server
 *
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.general.GetCurrentTime
 * --url [webservicesurl] --username [Username] --password [password]
 * </pre>
 */
@Sample(
        name = "get-current-time",
        description = "This sample gets the current time of the vSphere Server"
)
public class GetCurrentTime extends ConnectedVimServiceBase {
    /**
     * This method retrieves the current time from the server and prints it.
     *
     * @throws RuntimeFaultFaultMsg
     */
    @Action
    public void getCurrentTime() throws RuntimeFaultFaultMsg {
        XMLGregorianCalendar ct = vimPort.currentTime(this.getServiceInstanceReference());
        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm:ss.SSSZ");
        System.out.println("Server current time: "
                + sdf.format(ct.toGregorianCalendar().getTime()));
    }
}