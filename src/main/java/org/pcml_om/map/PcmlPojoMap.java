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
				peList.add(struct.getElement(peName).clone(struct.getPreamble()));
			}
		}
		PcmlElement[] pcmlElements = new PcmlElement[peList.size()];
		pcmlElements = peList.toArray(pcmlElements);
		pcmlElementMap.put(progName, pcmlElements);
	}
	
	public PcmlElement[] getPcmlElements(String programName) {
		return pcmlElementMap.get(programName);
	}
	
	private String defaultPcmlDateFmt = "yyyyMMdd";

	public void setPcmlDefaultDateFormat(String format) {
		defaultPcmlDateFmt = format;
	}

	public String getPcmlDefaultDateFormat() {
		return defaultPcmlDateFmt;
	}

//	private String defaultPojoDateFmt = "yyyyMMdd";
	private String defaultPojoDateFmt = "MM/dd/yyyy";

	public void setPojoDefaultDateFormat(String format) {
		defaultPojoDateFmt = format;
	}

	public String getPojoDefaultDateFormat() {
		return defaultPojoDateFmt;
	}

}
