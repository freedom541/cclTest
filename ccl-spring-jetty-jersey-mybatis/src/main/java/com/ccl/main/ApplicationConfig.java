package com.ccl.main;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;


public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig() {
    	packages("com.ccl.action");
        register(JacksonFeature.class);
        //register(RequestContextFilter.class);  // Though it might be needed. Guess not
        property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
    }
}
