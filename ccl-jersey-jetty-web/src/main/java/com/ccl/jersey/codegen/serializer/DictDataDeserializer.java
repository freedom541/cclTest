package com.ccl.jersey.codegen.serializer;


import com.ccl.jersey.codegen.DataTypeConvertUtils;
import com.ccl.jersey.codegen.DictData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ccl
 * @date 2016/1/29.
 */
public class DictDataDeserializer<T extends DictData> extends JsonDeserializer<T> {

    Class<?> dictDataType;

    public DictDataDeserializer() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) superType;
            Type cmpType = ptype.getActualTypeArguments()[0];
            this.dictDataType = (Class) cmpType;
        }
    }

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        int intValue = jp.getIntValue();
        return (T) DataTypeConvertUtils.convert(intValue, dictDataType);
    }
}
