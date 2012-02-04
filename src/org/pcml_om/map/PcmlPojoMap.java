package org.pcml_om.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PcmlPojoMap {
	
	private Map<String, Map<String, PcmlStruct>> pcmlMap;
	private Map<String, PcmlElement[]> pcmlElementMap;

	
	public PcmlPojoMap() {
		pcmlMap = new HashMap<String, Map<String,PcmlStruct>>();
		pcmlElementMap = new HashMap<String, PcmlElement[]>();
	}
	
	public Map<String, PcmlStruct> getPcmlStructMap(String programName) {
		return pcmlMap.get(programName);
	}
	
	public void putPcmlStructMap(String progName,
			Map<String, PcmlStruct> progStructMap) {
		pcmlMap.put(progName, progStructMap);
		List<PcmlElement> peList = new ArrayList<PcmlElement>();
		Collection<PcmlStruct> structCollection = progStructMap.values();
		for (PcmlStruct struct : structCollection) {
			String[] peNames = struct.getElementNames();
			for (String peName : peNames) {
				peList.add(struct.getElement(peName));
			}
		}
		PcmlElement[] pcmlElements = new PcmlElement[peList.size()];
		pcmlElements = peList.toArray(pcmlElements);
		pcmlElementMap.put(progName, pcmlElements);
	}
	
	public PcmlElement[] getPcmlElements(String programName) {
		return pcmlElementMap.get(programName);
	}
		
	private String defaultDateFormat = "yyyyMMdd";
	public void setDefaultDateFormat(String format) {
		defaultDateFormat = format;
	}
	public String getDefaultDateFormat() { 
		return defaultDateFormat;
	}

}
