package com.vmware.connection;

import com.vmware.common.annotations.Before;
import com.vmware.connection.helpers.ApiValidator;

/**
 * Some API samples only make sense when used with an ESX(i) Host
 * Samples based on this class will only function when connected
 * to an ESX/ESXi host.
 */
public class ESXHostSampleBase extends ConnectedVimServiceBase {

    @Override
    public void setConnection(final Connection rawConnection) {
        System.out.println("Forcing basic connection type: this sample is intended for use with an ESX or ESXi host.");
        super.setConnection(basicConnectionFromConnection(rawConnection));
    }

    @Before
    @Override
    public Connection connect() {
        super.connect();
        if(!ApiValidator.assertHost(connection)) {
        	System.out.printf("exiting early%n");
            connection.disconnect();
            // Not the best form, but without a connection to an ESX(i) the sample is useless.
            System.exit(0);
        }
        return this.connection;
    }
}
