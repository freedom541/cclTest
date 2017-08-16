package com.ccl.jersey.jettyServer;

/**
 * Created by ccl on 17/8/14.
 */

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;


public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        packages("com.ccl.jersey.action");
        register(JacksonFeature.class);
        //register(RequestContextFilter.class);  // Though it might be needed. Guess not
        property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
    }
}
