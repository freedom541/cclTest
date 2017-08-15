package com.ccl.jersey.codegen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.Map.Entry;

@XmlRootElement
@Label("数据映射集")
public class SimpleDataMap extends ArrayList<SimpleDataMap.DataEntry> implements
		DataMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 902996385230541320L;

	public SimpleDataMap() {
		super();
	}

	public SimpleDataMap(Collection<? extends DataEntry> c) {
		super(c);
	}

	public SimpleDataMap(int initialCapacity) {
		super(initialCapacity);
	}

	@Override
	public boolean add(DataEntry e) {
		// 过滤空值
		if (null == e) {
			return false;
		}
		return super.add(e);
	}

	@Override
	public void add(int index, DataEntry element) {
		// 过滤空值
		if (null == element) {
			return;
		}
		super.add(index, element);
	}

	@Override
	public void putAll(DataMap<String, Object> dataMap) {
		if (null != dataMap) {
			Set<Entry<String, Object>> entrySet = dataMap.entrySet();
			for (Entry<String, Object> entry : entrySet) {
				this.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	@Label("数据映射实体")
	@JsonInclude(Include.NON_NULL)
	public static class DataEntry implements
			Entry<String, Object> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6560112567075973046L;

		@Label("键")
		private String key;

		@Label("值")
		private Object value;

		public DataEntry() {
		}

		public DataEntry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public Object setValue(Object value) {
			Object old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DataEntry other = (DataEntry) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return String.format("{key:%s, value:%s}", key, value);
		}

	}

	@Override
	public boolean containsKey(String key) {
		if (null == key) {
			for (DataEntry dataEntry : this) {
				if (null == dataEntry.key) {
					return true;
				}
			}
		} else {
			for (DataEntry dataEntry : this) {
				if (key.equals(dataEntry.key)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		if (null == value) {
			for (DataEntry dataEntry : this) {
				if (null == dataEntry.value) {
					return true;
				}
			}
		} else {
			for (DataEntry dataEntry : this) {
				if (value.equals(dataEntry.value)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object get(String key) {
		if (null == key) {
			for (DataEntry dataEntry : this) {
				if (null == dataEntry.key) {
					return dataEntry.value;
				}
			}
		} else {
			for (DataEntry dataEntry : this) {
				if (key.equals(dataEntry.key)) {
					return dataEntry.value;
				}
			}
		}
		return null;
	}

	@Override
	public Object put(String key, Object value) {
		Object old = get(key);
		DataEntry dataEntry = new DataEntry(key, value);
		this.add(dataEntry);
		return old;
	}

	@Override
	public Object removeByKey(String key) {
		Object old = get(key);
		Iterator<DataEntry> iterator = this.iterator();
		while (iterator.hasNext()) {
			DataEntry dataEntry = (DataEntry) iterator.next();
			if (null == key) {
				if (null == dataEntry.key) {
					iterator.remove();
				}
			} else {
				if (key.equals(dataEntry.key)) {
					iterator.remove();
				}
			}
		}
		return old;
	}

	@Override
	public void putAll(Map<String, Object> m) {
		if (null != m) {
			for (Entry<String, Object> entry : m.entrySet()) {
				DataEntry dataEntry = new DataEntry(entry.getKey(), entry.getValue());
				this.add(dataEntry);
			}
		}

	}

	@Override
	public Set<String> keySet() {
		Set<String> keySet = new HashSet<>();
		for (DataEntry dataEntry : this) {
			keySet.add(dataEntry.key);
		}
		return keySet;
	}

	@Override
	public Collection<Object> values() {
		Collection<Object> values = new ArrayList<>();
		for (DataEntry dataEntry : this) {
			values.add(dataEntry.value);
		}
		return values;
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		Set<Entry<String, Object>> entrySet = new HashSet<>();
		for (DataEntry dataEntry : this) {
			entrySet.add(dataEntry);
		}
		return entrySet;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		for (DataEntry dataEntry : this) {
			map.put(dataEntry.key, dataEntry.value);
		}
		return map;
	}

}
