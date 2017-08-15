package com.ccl.jersey.codegen;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ccl on 2015/7/20.
 * <p>
 * 输出业务日志
 */
public class BizLog {
    private Logger logger;

    private static List<LogStorageService> logStorageServices = new ArrayList<>();

    private static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private BizLog(Class clazz) {
        logger = LoggerFactory
                .getLogger(clazz);
    }

    public static BizLog getLogger(Class clazz) {
        return new BizLog(clazz);
    }

    public void mark(String event, boolean success, String objectType, String objectId, String... tag) {
        mark(null, event, success, objectType, objectId, Arrays.asList(tag));
    }

    public void mark(String user, String event, boolean success, String objectType, String objectId, String... tag) {
        mark(user, event, success, objectType, objectId, Arrays.asList(tag));
    }


    private void mark(String user, String event, boolean success, String objectType, String objectId, List<String> tag) {
        LogMetadata logMetadata = new LogMetadata(event, success, objectType, objectId, tag);
        if (StringUtils.isBlank(user)) {
            user = "ccl";
        }
        logMetadata.setUser(user);
        logMetadata.setRemoteHost(RequestHeaderUtils.getRequestHeaders().get("Remote-Host"));

        for (LogStorageService logStorageService : logStorageServices) {
            threadPool.submit((Runnable) () -> logStorageService.saveLog(logMetadata));
        }
        try {
            logger.info(MarkerFactory.getMarker(LogMarker.BUSINESS), logMetadata.toString());
        } catch (Exception e) {
            e.printStackTrace();
            //ignore
        }
    }


    public static void addLogStorageService(LogStorageService logStorageService) {
        logStorageServices.add(logStorageService);
    }

}
