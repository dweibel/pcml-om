package org.pcml_om.map;

public class PojoElement {
	private String pcmlName;
	private String pojoFieldName;
	private Class<?> pojoFieldType;
	private String convertToLegacy;
	private String convertToPojo;
	private String legacyFormat;
	private String pojoFormat;
	public String getPcmlName() {
		return pcmlName;
	}
	public void setPcmlName(String pcmlName) {
		this.pcmlName = pcmlName;
	}
	public String getPojoFieldName() {
		return pojoFieldName;
	}
	public void setPojoFieldName(String pojoFieldName) {
		this.pojoFieldName = pojoFieldName;
	}
	public Class<?> getPojoFieldType() {
		return pojoFieldType;
	}
	public void setPojoFieldType(Class<?> pojoFieldType) {
		this.pojoFieldType = pojoFieldType;
	}
	public String getConvertToLegacy() {
		return convertToLegacy;
	}
	public void setConvertToLegacy(String convertToLegacy) {
		this.convertToLegacy = convertToLegacy;
	}
	public String getConvertToPojo() {
		return convertToPojo;
	}
	public void setConvertToPojo(String convertToPojo) {
		this.convertToPojo = convertToPojo;
	}
	public String getLegacyFormat() {
		return legacyFormat;
	}
	public void setLegacyFormat(String legacyFormat) {
		this.legacyFormat = legacyFormat;
	}
	public String getPojoFormat() {
		return pojoFormat;
	}
	public void setPojoFormat(String pojoFormat) {
		this.pojoFormat = pojoFormat;
	}
	public String getClassName() {
		return getClassName(pojoFieldName);
	}
	public String getFieldName() {
		return getFieldName(pojoFieldName);
	}
	
	public static String getClassName(String classField) {
		return classField.substring(0, classField.lastIndexOf('.'));
	}
	
	public static String getFieldName(String classField) {
		return classField.substring(classField.lastIndexOf('.')+1);
	}
	
}
