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

package com.vmware.simpleagent;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.Connection;
import com.vmware.connection.ConnectionFactory;
import com.vmware.security.credstore.CredentialStore;
import com.vmware.security.credstore.CredentialStoreAdmin;
import com.vmware.security.credstore.CredentialStoreFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.util.Set;

/**
 * <pre>
 * SimpleAgent
 *
 * The SimpleAgent class uses the local credential store to obtain user account
 * and password information, for automated logon to the target host system.
 * SimpleAgent can be used with {@link CreateUser}, to
 * demonstrate using the {@link CredentialStore} client API.
 * SimpleAgent accesses the local credential store to obtain a single user
 * account to login to the specified server (--hostName is the only common-line
 * argument). If more than one user account exists in the credential store,
 * an error message displays.
 * To create user accounts and store them in the local credential store, use
 * the {@link CredentialStoreAdmin} client utility.
 *
 * <b>Parameters:</b>
 * hostName           [required] : The fully-qualified domain name of the server
 *
 * <b>Command Line:</b>
 * run.bat com.vmware.simpleagent.SimpleAgent --hostName [myServerName]
 * </pre>
 */
@Sample(name = "simple-agent", description = "" +
        "The SimpleAgent class uses the local credential store to obtain user account " +
        "and password information, for automated logon to the target host system. " +
        "SimpleAgent can be used with CreateUser, to " +
        "demonstrate using the CredentialStore client API. " +
        "SimpleAgent accesses the local credential store to obtain a single user " +
        "account to login to the specified server (--hostName is the only common-line " +
        "argument). If more than one user account exists in the credential store, " +
        "an error message displays. " +
        "To create user accounts and store them in the local credential store, use " +
        "the CredentialStoreAdmin client utility. "
)
public class SimpleAgent {

    String hostName = null;

    @Option(name = "hostname", description = " The fully-qualified domain name of the server")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @Action
    public void connectAndLogin() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, IllegalBlockSizeException, BadPaddingException {
        Connection connection = ConnectionFactory.newConnection();
        CredentialStore csObj = CredentialStoreFactory.getCredentialStore();

        String userName = "";
        Set<String> userNames = csObj.getUsernames(hostName);
        if (userNames.size() == 0) {
            System.out.println("No user found in this host");
            return;
        } else if (userNames.size() > 1) {
            System.out.println("Found two users for this host");
            return;
        } else {
            Object[] names = userNames.toArray();
            userName = (String) names[0];
        }

        String url = "https://" + hostName + "/sdk/vimService";
        char[] arr = csObj.getPassword(hostName, userName);
        String password = new String(arr);

        connection.setUrl(url);
        connection.setUsername(userName);
        connection.setPassword(password);
        connection.connect();


        System.out.println("Connected Successfully "
                + connection.getServiceContent().getAbout().getFullName());

        connection.disconnect();
    }


}
