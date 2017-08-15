package com.ccl.jersey.codegen;

public class SimpleDictData implements DictData {

	protected String value;

	protected String label;

	public SimpleDictData() {
		super();
	}

	public SimpleDictData(String value, String label) {
		super();
		this.value = value;
		this.label = label;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return String.format("SimpleDictData [value=%s, label=%s]",
                value, label);
	}

}
