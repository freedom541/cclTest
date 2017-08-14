package com.ccl.jersey;

/**
 * Created by ccl on 17/8/10.
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

public class SpringInit  {
    private  static ApplicationContext applicationContext ;
    private static ConfigLoader configLoad ;

    public  static void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        SpringInit.applicationContext = applicationContext;
        configLoad = (ConfigLoader)SpringInit.applicationContext.getBean("configLoader");

    }

    public static  ConfigLoader getConfigLoad() {
        return configLoad;
    }

    public void setConfigLoad(ConfigLoader configLoad) {
        SpringInit.configLoad = configLoad;
    }

    public  static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}

