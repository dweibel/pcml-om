package org.pcml_om.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PcmlElement {
	public static final String CHAR = "char";
	public static final String INT = "int";
	public static final String PACKED = "packed";
	public static final String ZONED = "zoned";
	public static final String FLOAT = "float";
	public static final String BYTE = "byte";
	public static final String STRUCT = "struct";
	private static final Set<String> typeSet = new HashSet<String>();
	{
		typeSet.add(CHAR);
		typeSet.add(INT);
		typeSet.add(PACKED);
		typeSet.add(ZONED);
		typeSet.add(FLOAT);
		typeSet.add(BYTE);
		typeSet.add(STRUCT);
	}
	
	private String preamble = "";
	private String name = "";
	private String type = "";
	private int length = 0;
	private int precision = 0;
	private int count = 1;
	private boolean outputOnly = false;
		
	private PojoElement pojoElement;
	private List<PcmlElement> pcmlSubstruct = null;
	private String pcmlSubstructName = "";
	
	public void setPreamble(String preamble) {
		this.preamble = preamble;
	}
	
	public String getQualifiedName() {
		return preamble + name;
	}
	
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
	public List<PcmlElement> getPcmlSubstruct() {
		return pcmlSubstruct;
	}
	public void setPcmlSubtruct(List<PcmlElement> pcmlStruct) {
		this.pcmlSubstruct = pcmlStruct;
	}
	public String getPcmlSubstructName() {
		return pcmlSubstructName;
	}
	public void setPcmlSubtructName(String pcmlStructName) {
		this.pcmlSubstructName = pcmlStructName;
	}
	public boolean isOutputOnly() {
		return outputOnly;
	}
	public void setOutputOnly() {
		this.outputOnly = true;
	}
	
	public PcmlElement clone(String preamble) {
		PcmlElement result = new PcmlElement();
		result.preamble = preamble;
		result.count = this.count;
		result.length = this.length;
		result.name = this.name;
		result.pojoElement = this.pojoElement;
		result.precision = this.precision;
		result.type = this.type;
		result.outputOnly = this.outputOnly;
		if (STRUCT.equals(this.type) && this.pcmlSubstruct != null) {
			result.pcmlSubstructName = this.pcmlSubstructName;
			result.pcmlSubstruct = new ArrayList<PcmlElement>();
			for (PcmlElement pe : pcmlSubstruct) {
				result.pcmlSubstruct.add(pe.clone(result.getQualifiedName() + "."));
			}
		}
		return result;
	}

}
