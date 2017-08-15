package com.ccl.jersey.codegen;

/**
 * @author ccl
 * @date 2016/11/2.
 */
public interface LogStorageService {
    /**
     * 保存日誌
     *
     * @param logMetadata
     */
    void saveLog(LogMetadata logMetadata);
}
