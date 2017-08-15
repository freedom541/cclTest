package com.ccl.jersey.codegen;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author ccl
 * @date 2016/1/14.
 */
public class BeanHelper {
    /**
     * 判断是否为基本数据类型
     *
     * @param type
     * @return
     */
    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() || Byte.class.equals(type) || Long.class.equals(type)
                || Integer.class.equals(type) || Short.class.equals(type)
                || Float.class.equals(type) || Double.class.equals(type)
                || BigDecimal.class.equals(type) || Date.class.equals(type)
                || Timestamp.class.equals(type) || LocalDate.class.equals(type) || LocalTime.class.equals(type)
                || DateTime.class.equals(type) || Boolean.class.equals(type)
                || String.class.equals(type);
    }
}
