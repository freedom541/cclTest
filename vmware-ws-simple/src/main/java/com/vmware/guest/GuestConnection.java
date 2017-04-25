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

package com.vmware.guest;

import com.vmware.common.annotations.Option;

/**
 * vSphere API 5.0 is the first version to understand Guest connections.
 */
public class GuestConnection {
    String vmname;
    String username;
    String password;

    @Option(name = "vmname", description = "name of guest vm to connect to")
    public void setVmname(String vm) {
        this.vmname = vm;
    }

    @Option(name = "guestusername", description = "username on guest vm")
    public void setUsername(String username) {
        this.username = username;
    }

    @Option(name = "guestpassword", description = "password on guest vm")
    public void setPassword(String password) {
        this.password = password;
    }
}
