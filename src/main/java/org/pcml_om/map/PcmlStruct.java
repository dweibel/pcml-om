package org.pcml_om.map;

import org.pcml_om.PcmlCallException;

import java.util.HashMap;
import java.util.Map;

import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;

public class PcmlStruct {
	
	private PcmlElement[] elements;
	private String[] shortElementNames;
	private String[] longElementNames;
	private String preamble;
 
	public PcmlStruct(String programName, String structName, PcmlElement[] elements) {
		if (structName.equals('[' + programName + ']')) {
			preamble = programName + '.';
		} else if (elements.length==1 || elements[0].getCount() > 1) {
			// this is a repeating structure
			preamble = programName + '.';
		} else {
			preamble = programName + '.' + structName + '.';
		}
		this.elements = elements;
		shortElementNames = new String[elements.length];
		longElementNames = new String[elements.length];
		for (int x=0; x<elements.length; x++) {
			String shortName = elements[x].getName();
			shortElementNames[x] = shortName;
			longElementNames[x] = preamble + shortName;
		}
	}
	
	public String[] getElementNames() {
		return shortElementNames;
	}
	
	public String getPreamble() {
		return preamble;
	}
	
	public PcmlElement getElement(String name) {
		boolean found = false;
		PcmlElement result = null;
		for (int x=0; x<elements.length && !found; x++) {
			if (name.equalsIgnoreCase(elements[x].getName())) {
				result = elements[x];
				found = true;
		 	}
		}
		return result;
	}
	
	private void checkElemValues(Object[] elementValues) throws PcmlCallException {
		if (elementValues.length != elements.length) {
			throw new PcmlCallException(
					"The number of element values do not equal the number of element names; " +
					"Names="  + elements.length + ", Values=" + elementValues.length);
		}
	}
	
	public void setDocValues(ProgramCallDocument pcd, Object[] elementValues) throws PcmlCallException {
		checkElemValues(elementValues);
		String elementName = null;
		Object elementValue = null;
		try {
			for (int x=0; x<elements.length; x++) {
				elementName = longElementNames[x];
				elementValue = elementValues[x];
				pcd.setValue(elementName, elementValue);
			}
		} catch (Exception e) {
			String msg = String.format(
					"Unable to set ProgramCallDocument element:%s, value:%s", 
					elementName, elementValue);
			throw new PcmlCallException(msg, e);
		}
	}
	
	public Map<String, Object> getDocValues(ProgramCallDocument pcd) throws PcmlCallException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			for (int x=0; x<elements.length; x++) {
				Object value = pcd.getValue(longElementNames[x]);
				resultMap.put(elements[x].getName(), value);
			}
			return resultMap;
		} catch (PcmlException e) {
			throw new PcmlCallException("Unable to get ProgramCallDocument values", e);
		}
	}
	
	public void setDocValues(ProgramCallDocument pcd, int index, Object[] elementValues) throws PcmlCallException {
		int[] indices = { index };
		setDocValues(pcd, indices, elementValues);
	}
	
	public Map<String, Object> getDocValues(ProgramCallDocument pcd, int index) throws PcmlCallException {
		int[] indices = { index };
		return getDocValues(pcd, indices);
	}
	
	public void setDocValues(ProgramCallDocument pcd, int[] indices, Object[] elementValues) throws PcmlCallException {
		checkElemValues(elementValues);
		try {
			for (int x=0; x<elements.length; x++) {
				pcd.setValue(longElementNames[x], indices, elementValues[x]);
			}
		} catch (PcmlException e) {
			throw new PcmlCallException("Unable to set ProgramCallDocument values", e);
		}
	}
	
	public Map<String, Object> getDocValues(ProgramCallDocument pcd, int[] indices) throws PcmlCallException {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			for (int x=0; x<elements.length; x++) {
				Object value = pcd.getValue(longElementNames[x], indices);
				resultMap.put(elements[x].getName(), value);
			}
		} catch (PcmlException e) {
			throw new PcmlCallException("Unable to get ProgramCallDocument values", e);
		}
		return resultMap;
	}
	
	public Object getValue(ProgramCallDocument pcd, String element) throws PcmlCallException {
		Object result = null;
		try {
			result = pcd.getValue(preamble + element);
		} catch (PcmlException e) {
			throw new PcmlCallException("Unable to get ProgramCallDocument value", e);
		}
		return result;
	}
	
	public Object getValue(ProgramCallDocument pcd, String element, int index) throws PcmlCallException {
		int[] indices = { index };
		return getValue(pcd, element, indices);
	}

	private Object getValue(ProgramCallDocument pcd, String element, int[] indices) throws PcmlCallException {
		Object result = null;
		try {
			result = pcd.getValue(preamble + element, indices);
		} catch (PcmlException e) {
			throw new PcmlCallException("Unable to get ProgramCallDocument value", e);
		}
		return result;
	}

}
