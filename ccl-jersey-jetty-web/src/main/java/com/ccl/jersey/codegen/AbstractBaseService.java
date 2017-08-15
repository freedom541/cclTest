package com.ccl.jersey.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public abstract class AbstractBaseService implements Service {

    protected final Logger sysLogger = LoggerFactory.getLogger(this.getClass());

    protected final BizLog logger = BizLog.getLogger(this.getClass());

    @PostConstruct
    public void startService() {
        sysLogger.debug(MarkerFactory.getMarker(LogMarker.CONTAINER), getClass().getSimpleName() + " starting...");
        init();
        if (this instanceof LogStorageService) {
            BizLog.addLogStorageService((LogStorageService) this);
        }
        sysLogger.debug(MarkerFactory.getMarker(LogMarker.CONTAINER), getClass().getSimpleName() + " start success.");
    }

    @PreDestroy
    public void stopService() {
        sysLogger.debug(MarkerFactory.getMarker(LogMarker.CONTAINER), getClass().getSimpleName() + " stopping...");
        clean();
        sysLogger.debug(MarkerFactory.getMarker(LogMarker.CONTAINER), getClass().getSimpleName() + " stop success.");
    }

    protected void init() {
    }

    protected void clean() {
    }

}
