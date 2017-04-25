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
import com.vmware.connection.ESXHostSampleBase;
import com.vmware.security.credstore.CredentialStore;
import com.vmware.security.credstore.CredentialStoreFactory;
import com.vmware.vim25.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * CreateUser
 *
 * The CreateUser class creates a user account and password stores them the local  credential store.
 * CreateUser should be used in conjunction with the  {@link SimpleAgent} sample application only.
 * CreateUser generates an arbitrary user name (Usernnnn, where nnnn is  a psuedo-randomly generated number)
 * and password and stores them in the credential store on the local machine.
 * CreateUser works with ESX or ESXi only (not vCenter)
 *
 * <b>Parameters:</b>
 * url              [required] : url of the web service
 * username         [required] : username for the authentication
 * password         [required] : password for the authentication
 * server           [required] : server for which username and password are to be added
 * user             [optional] : provide your own username to create (default is a randomly generated name)
 * pass             [optional] : provide your own password for newly created username
 *                               (default is a randomly generated password)
 * <b>Command Line:</b>
 * run.bat com.vmware.simpleagent.CreateUser --server [myServerName] --url [URLString]
 * --username [User] --password [Password]
 * </pre>
 */
@Sample(
        name = "create-user",
        description = "\n\n" +
                "CreateUser generates an random username " +
                "(Usernnnn, where nnnn is a psuedo-randomly generated number) " +
                "and random password and stores them in the credential store on the local machine. " +
                "CreateUser works with ESX or ESXi only (not vCenter) " +
                "\n"
)
public class CreateUser extends ESXHostSampleBase {
    private String userName;
    private String password;

    String server = null;

    @Option(name = "server", required = false, description = "server for which username and password are to be added")
    public void setServer(String server) {
        this.server = server;
    }

    @Option(name="user",required = false,
            description = "provide your own username to create (default is a randomly generated name)")
    public void setUser(String userName) {
        this.userName = userName;
    }

    public String getUser() {
        return (userName == null) ? generateUserName():userName;
    }

    @Option(name="pass",required = false,
            description = "provide your own password for newly created username " +
                    "(default is a randomly generated password)")
    public void setPass(String password) {
        this.password = password;
    }

    public String getPass() {
        return (password == null) ? generatePassword():password;
    }

    String generateUserName() {
        int rawRandomNumber = (int) (Math.random() * (256 - 32 + 1)) + 32;
        String user = "user" + Integer.toString(rawRandomNumber);
        return user;
    }


    String generatePassword() {
        int rawRandomNumber = (int) (Math.random() * (256 - 32 + 1)) + 32;
        String passwd = "passwd" + Integer.toString(rawRandomNumber);
        return passwd;
    }

    String getServerName() {
        if (server != null) {
            return server;
        } else {
            String urlString = connection.getUrl();
            if (urlString.indexOf("https://") != -1) {
                int sind = 8;
                int lind = urlString.indexOf("/sdk");
                return urlString.substring(sind, lind);
            } else if (urlString.indexOf("http://") != -1) {
                int sind = 7;
                int lind = urlString.indexOf("/sdk");
                return urlString.substring(sind, lind);
            } else {
                return urlString;
            }
        }
    }

    @Action
    public void createUser() throws RuntimeFaultFaultMsg, AlreadyExistsFaultMsg, NotFoundFaultMsg, AuthMinimumAdminPermissionFaultMsg, UserNotFoundFaultMsg, IOException {
        vimPort.currentTime(this.getServiceInstanceReference());

        ManagedObjectReference hostLocalAccountManager =
                serviceContent.getAccountManager();


        ManagedObjectReference hostAuthorizationManager =
                serviceContent.getAuthorizationManager();

        String user = getUser();
        String pass = getPass();

        HostAccountSpec hostAccountSpec = new HostAccountSpec();
        hostAccountSpec.setId(user);
        hostAccountSpec.setPassword(pass);
        hostAccountSpec.setDescription("User Description");
        vimPort.createUser(hostLocalAccountManager, hostAccountSpec);

        ManagedObjectReference rootFolder = serviceContent.getRootFolder();

        /* For demonstration purposes only, the account is granted
          the 'administrator' role (-1) on the rootFolder of the inventory.
         Never give users more privileges than absolutely necessary.
        */

        Permission per = new Permission();
        per.setGroup(false);
        per.setPrincipal(user);
        per.setRoleId(-1);
        per.setPropagate(true);
        per.setEntity(rootFolder);
        List<Permission> permissions = new ArrayList<Permission>();
        permissions.add(per);
        vimPort.setEntityPermissions(hostAuthorizationManager, rootFolder,
                permissions);


        CredentialStore csObj = CredentialStoreFactory.getCredentialStore();
        csObj.addPassword(getServerName(), user, pass.toCharArray());
        System.out.println("Successfully created user and populated the "
                + "credential store");
    }

}
