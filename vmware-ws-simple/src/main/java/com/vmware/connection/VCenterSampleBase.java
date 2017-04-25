package com.vmware.connection;

import com.vmware.common.annotations.Before;
import com.vmware.connection.helpers.ApiValidator;

/**
 * Some samples must use a VCenter to make any sense. For example
 * an inventory browser makes no sense on an ESX host. Samples
 * based on this base class should not be used with non vCenter
 * servers.
 */
public class VCenterSampleBase extends ConnectedVimServiceBase {
    @Before
    @Override
    public Connection connect() {
        super.connect();
        if(!ApiValidator.assertVCenter(connection)) {
            System.out.printf("exiting early%n");
            connection.disconnect();
            // Not the best form, but without a connection to a VCenter the sample is useless.
            System.exit(0);
        }
        return this.connection;
    }
}
