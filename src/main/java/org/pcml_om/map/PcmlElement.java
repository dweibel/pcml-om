package org.pcml_om.map;

import java.util.HashSet;
import java.util.Set;

public class PcmlElement {
	public static final String CHAR = "char";
	public static final String INT = "int";
	public static final String PACKED = "packed";
	public static final String ZONED = "zoned";
	public static final String FLOAT = "float";
	public static final String BYTE = "byte";
	private static final Set<String> typeSet = new HashSet<String>();
	{
		typeSet.add(CHAR);
		typeSet.add(INT);
		typeSet.add(PACKED);
		typeSet.add(ZONED);
		typeSet.add(FLOAT);
		typeSet.add(BYTE);
	}
	
	private String name = "";
	private String type = "";
	private int length = 0;
	private int precision = 0;
	private int count = 0;
	
	private PojoElement pojoElement;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if (typeSet.contains(type)) {
			this.type = type;
		} else {
			throw new NullPointerException("Invalid PcmlElement Type: " + type);
		}
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getPrecision() {
		return precision;
	}
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public PojoElement getPojoElement() {
		return pojoElement;
	}
	public void setPojoElement(PojoElement pojoElement) {
		this.pojoElement = pojoElement;
	}

}
