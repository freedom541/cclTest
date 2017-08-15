package com.ccl.jersey.codegen;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 分页请求实现
 * <p>
 * User: ccl.lu Date: 13-9-9 Time: 上午10:35
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Label("分页条件")
public class PageRequest implements BaseBean {
    private static final long serialVersionUID = 8280485938848398236L;

    @NotNull
    @Label("页码")
    private int page;
    @NotNull
    @Label("页大小")
    private int size;

    public PageRequest() {
        this(0, 0);
    }


    public PageRequest(int page, int size) {
        super();
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page < 1 ? 1 : page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size <= 0 ? 20 : size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }

    @Override
    public String toString() {
        return "{ page=" + getPage() + ", size=" + size + " }";
    }

}
