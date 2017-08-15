package com.ccl.jersey.codegen;

import java.io.Serializable;

/**
 * 数据空值
 * 
 * @author ccl
 * 
 */
public class EmptyValue implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 447418066855018754L;

	@Override
	public String toString() {
		return String.format("{NULL}");
	}

}