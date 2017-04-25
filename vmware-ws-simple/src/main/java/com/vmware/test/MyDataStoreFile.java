package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.Map;

/**
 * Created by ccl on 17/3/31.
 */
public class MyDataStoreFile extends ConnectedVimServiceBase {
    public void getfiles() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, GuestOperationsFaultFaultMsg, FileFaultFaultMsg, TaskInProgressFaultMsg, InvalidStateFaultMsg {

        Map<String,ManagedObjectReference> inFolderByType = getMOREFs.inFolderByType(serviceContent.getRootFolder(),"Datastore");



        ManagedObjectReference vmr = getMOREFs.vmByVMname("all",serviceContent.getPropertyCollector());
        String path = "[datastore1] allll";
        GuestListFileInfo info = vimPort.listFilesInGuest(serviceContent.getFileManager(),vmr,null,path,null,null,null);

        System.out.println("OK");
    }
}
