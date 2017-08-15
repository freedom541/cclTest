package com.ccl.jersey.codegen;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.converters.SqlTimestampConverter;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 數據類型轉換方便類
 *
 * @author ccl
 * @date 2016/1/29.
 */
public class DataTypeConvertUtils {
    static {
        SqlTimestampConverter sqlTimestampConverter = new SqlTimestampConverter();
        sqlTimestampConverter.setPatterns(new String[]{"yyyy-MM-dd",
                "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss.S",
                "yyyy-MM-dd'T'HH:mm:ss.S"});
        ConvertUtils.register(sqlTimestampConverter, Timestamp.class);
        ConvertUtils.register(sqlTimestampConverter, Date.class);

        //轉換字符串為字節數組
        Converter stringToByteArrayConverter = new Converter() {

            @SuppressWarnings("rawtypes")
            @Override
            public Object convert(Class type, Object value) {
                if (null == value)
                    return null;
                if (value instanceof String) {
                    try {
                        value = ((String) value).getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return value;
            }
        };
        ConvertUtils.register(stringToByteArrayConverter, byte[].class);

        //轉換字節數組為字符串
        Converter byteArrayToStringConverter = new Converter() {

            @SuppressWarnings("rawtypes")
            @Override
            public Object convert(Class type, Object value) {
                if (null == value)
                    return null;
                if (value instanceof byte[]) {
                    try {
                        value = new String((byte[]) value, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                return value;
            }
        };
        ConvertUtils.register(byteArrayToStringConverter, String.class);
    }

    /**
     * 轉換數據類型
     *
     * @param propertyValue
     * @param destType
     * @return
     */
    public static <T> T convert(
            Object propertyValue, Class<T> destType) {
        if (null == propertyValue)
            return null;

        Class<?> sourceType = propertyValue.getClass();
        //轉換字典成數字
        if (propertyValue instanceof DictData
                && String.class.equals(destType)) {
            DictData dictData = (DictData) propertyValue;
            return (T) dictData.getValue();
        }
        // 转换枚举值
        else if (sourceType.isEnum()) {
            Object[] enumConstants = sourceType.getEnumConstants();
            for (int i = 0; i < enumConstants.length; i++) {
                if (propertyValue.equals(enumConstants[i])) {
                    if (destType.equals(Integer.class)) {
                        propertyValue = Integer.valueOf(i);
                    } else {
                        propertyValue = ((Enum<?>) enumConstants[i]).name();
                    }
                    return (T) propertyValue;
                }
            }
        }

        //從數字轉換成字典
        if (DictData.class.isAssignableFrom(destType) && propertyValue instanceof String) {
            try {
                return (T) MethodUtils.invokeStaticMethod(destType, "fromValue", propertyValue);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Read dictionary [" + destType + "] 's fromValue method error.");
            }
        }
        // 转换成枚举值
        else if (destType.isEnum()) {
            Object[] enumConstants = destType.getEnumConstants();
            for (int i = 0; i < enumConstants.length; i++) {
                if (sourceType.equals(Integer.class)) {
                    if (propertyValue.equals(i)) {
                        propertyValue = enumConstants[i];
                        return (T) propertyValue;
                    }
                } else {
                    if (propertyValue.equals(((Enum<?>) enumConstants[i]).name())) {
                        propertyValue = enumConstants[i];
                        return (T) propertyValue;
                    }
                }

            }
        }
        propertyValue = ConvertUtils.convert(propertyValue, destType);
        return (T) propertyValue;
    }
}
