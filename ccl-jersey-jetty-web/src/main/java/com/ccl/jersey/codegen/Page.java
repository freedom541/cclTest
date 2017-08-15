package com.ccl.jersey.codegen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Page<T> implements Iterable<T>, Serializable {

    public final static int MAX_RECORDS = 1000;

    private List<T> content = new ArrayList<T>();
    private long totalElements;
    private int size;
    private int page;
    private Sort sort;

    public Page() {
    }

    public Page(int page, int size) {
        this(null, page, size, null, 0);
    }

    public Page(List<T> content) {
        this(content, 0, 0, null, null == content ? 0 : content.size());
    }

    public Page(List<T> content, int page, int size, Sort sort, long total) {
        if (null != content) {
            this.content.addAll(content);
        }
        this.totalElements = total;
        this.size = size;
        this.page = page;
        this.sort = sort;
    }

    public int getSize() {
        return size == 0 ? MAX_RECORDS : size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page <= 0 ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public int getTotalPages() {
        int ceil = (int) Math.ceil((double) totalElements / (double) getSize());
        return getSize() == 0 ? 0 : ceil;
    }

    public int getNumberOfElements() {
        return content.size();
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public boolean hasPreviousPage() {
        return getPage() > 1;
    }

    public boolean isFirstPage() {
        return !hasPreviousPage();
    }

    public boolean hasNextPage() {
        return getPage() * getSize() < totalElements;
    }

    public boolean isLastPage() {
        return !hasNextPage();
    }

    public Iterator<T> iterator() {
        return content.iterator();
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public List<T> getContent() {
        return content;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }


    @Override
    public String toString() {

        String contentType = "UNKNOWN";

        if (content.size() > 0) {
            contentType = content.get(0).getClass().getName();
        }

        return String.format("Page %s of %d containing %d %s instances",
                getPage(), getTotalPages(), getNumberOfElements(), contentType);
    }

}
