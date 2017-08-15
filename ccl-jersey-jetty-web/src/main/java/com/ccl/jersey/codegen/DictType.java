package com.ccl.jersey.codegen;

import com.mysema.codegen.model.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ccl
 * @date 2016/1/28.
 */
public class DictType {
    private String name;

    private String label;

    private String table;

    private String column;

    private Type dictType;

    private List<DictItemType> items = new ArrayList<>();

    public DictType(String name, String label, String table, String column) {
        this.name = name;
        this.label = label;
        this.table = table;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public List<DictItemType> getItems() {
        return items;
    }

    public void setItems(List<DictItemType> items) {
        this.items = items;
    }

    public Type getDictType() {
        return dictType;
    }

    public void setDictType(Type dictType) {
        this.dictType = dictType;
    }
}
