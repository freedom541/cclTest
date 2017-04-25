package com.vmware.connection.helpers;

import com.vmware.connection.Connection;

/**
 * Some samples make use of API only found on vCenter. Other samples
 * make use of API that only make sense when used with a Host. This
 * utility helps with determining if the proper connection has been made.
 */
public class ApiValidator extends BaseHelper {
    public static final String VCENTER_API_TYPE = "VirtualCenter";
    public static final String HOST_API_TYPE = "HostAgent";

    public ApiValidator(final Connection connection) {
        super(connection);
    }

    public String getApiType() {
        return connection.connect().getServiceContent().getAbout().getApiType();
    }

    public boolean assertVCenter() {
        return isOfType(getApiType(),VCENTER_API_TYPE);
    }

    public boolean assertHost() {
        return isOfType(getApiType(),HOST_API_TYPE);
    }

    private boolean isOfType(final String apiType, final String vcenterApiType) {
        final Boolean same = vcenterApiType.equals(apiType);
        if(!same) {
            System.out.printf("This sample is currently connected to %s %n",apiType);
            System.out.printf("This sample should be used with %s %n", vcenterApiType);

        }
        return same;
    }

    public static boolean assertVCenter(final Connection connection) {
        return new ApiValidator(connection).assertVCenter();
    }

    public static boolean assertHost(final Connection connection) {
        return new ApiValidator(connection).assertHost();
    }
}
