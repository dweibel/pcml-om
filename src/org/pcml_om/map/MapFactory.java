package org.pcml_om.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MapFactory {

	private static Logger logger = Logger.getLogger(MapFactory.class.getName());
	
	Document pcmlDoc;
	Document pojoDoc;
	PcmlPojoMap pcmlPojoMap = new PcmlPojoMap();
	PojoReflectionHelper reflectionHelper = new PojoReflectionHelper();
	
	// preliminary PCML structure map 
	Map<String, List<PcmlElement>> pcmlStructMap = new HashMap<String, List<PcmlElement>>();

	private MapFactory(InputStream pcmlIs, InputStream pojoIs) throws ParserConfigurationException, SAXException, IOException {
		pcmlDoc = getDocument(pcmlIs);
		pojoDoc = getDocument(pojoIs);
		// create preliminary PCML structure map 
		loadPcmlStructMap(pcmlDoc);
		// load pojo elements into PCML structure map 
		loadPojos(pojoDoc);
	}
	
	/*
	 * Map<"Program Name", Map<"Structure Name", PcmlStructure>
	 */
	public static PcmlPojoMap getPcmlPojoMap(InputStream pcmlIs, InputStream pojoIs) 
			throws ParserConfigurationException, SAXException, IOException {
		MapFactory mf = new MapFactory(pcmlIs, pojoIs);
		return mf._getPcmlPojoMap();
	}
	
	private PcmlPojoMap _getPcmlPojoMap() 
			throws ParserConfigurationException, SAXException, IOException {
		
		// build final PCML map
		NodeList programNodes = pcmlDoc.getElementsByTagName("program");
		// iterate programs
		for (int x=0; x<programNodes.getLength(); x++) {
			Element progElement = (Element) programNodes.item(x);
			NodeList dataNodes = progElement.getChildNodes();
			String progName = progElement.getAttribute("name");
			Map<String, PcmlStruct> progStructMap = new HashMap<String, PcmlStruct>();
			// iterate program structures
			for (int y=0; y<dataNodes.getLength(); y++) {
				Node node = dataNodes.item(y);
				if (node instanceof Element) {
					Element data = (Element) node;
					if ("struct".equals(data.getAttribute("type"))) {
						String progStructName = data.getAttribute("name");
						String structName = data.getAttribute("struct");
						PcmlStruct pcmlStruct = createPcmlStruct(progName, structName, progStructName);
						progStructMap.put(structName, pcmlStruct);
					}
				}
			}
			// add the data elements not in a structure, but listed directly under program tag
			String structName = '[' + progName + ']';
			PcmlStruct pcmlStruct = createPcmlStruct(progName, structName, structName);
			progStructMap.put(structName, pcmlStruct);
			if (pcmlStruct!=null) {
				pcmlPojoMap.putPcmlStructMap(progName, progStructMap);
			}
		}
		return pcmlPojoMap;
	}

	private PcmlStruct createPcmlStruct(String progName, String structName, String progStructName) {
		PcmlStruct pcmlStruct = null;
		List<PcmlElement> peList = pcmlStructMap.get(structName);
		if (peList!=null) {
			PcmlElement[] peArray = new PcmlElement[peList.size()];
			peArray = peList.toArray(peArray);
			pcmlStruct = new PcmlStruct(progName, progStructName, peArray);
		}
		return pcmlStruct;
	}

	private void loadPcmlStructMap(Document doc) {
		NodeList structNodes = doc.getElementsByTagName("struct");
		loadPcmlStructs(structNodes, false);
		NodeList programNodes = doc.getElementsByTagName("program");
		loadPcmlStructs(programNodes, true);
	}

	private void loadPcmlStructs(NodeList structNodes, boolean programNodes) {
		for (int x=0; x<structNodes.getLength(); x++) {
			Element struct = (Element) structNodes.item(x);
			String structName = struct.getAttribute("name");
			if (programNodes) {
				structName = '[' + structName + ']';
			}
			NodeList dataNodes = struct.getChildNodes();
			List<PcmlElement> structElemList = new ArrayList<PcmlElement>();
			// iterate each structure element
			for (int y=0; y<dataNodes.getLength(); y++) {
				Node node = dataNodes.item(y);
				if (node!=null && node instanceof Element) {
					Element data = (Element) node;
					if (programNodes) {
						// when processing program nodes, ignore elements where type="struct"
						if (!"struct".equals(data.getAttribute("type"))) {
							structElemList.add(getPcmlElement(data));
						}
					} else {
						structElemList.add(getPcmlElement(data));
					}
				}
			}
			// After iterating through all elements in a single struct, 
			// store the list of elements under the struct name
			pcmlStructMap.put(structName, structElemList);
		}
	}

	private PcmlElement getPcmlElement(Element data) {
		PcmlElement pe = new PcmlElement();
		pe.setName(data.getAttribute("name"));
		pe.setType(data.getAttribute("type"));
		String length = data.getAttribute("length");
		if (length.length() > 0) {
			pe.setLength(Integer.parseInt(length));
		}
		String precision = data.getAttribute("precision");
		if (precision.length() > 0) {
			pe.setPrecision(Integer.parseInt(precision));
		}
		return pe;
	}
	
	private void loadPojos(Document doc) {
		NodeList structNodes = doc.getElementsByTagName("struct");
		loadPojoStructs(structNodes, false);
		NodeList programNodes = doc.getElementsByTagName("program");
		loadPojoStructs(programNodes, true);
	}

	private void loadPojoStructs(NodeList structNodes, boolean programNodes) {
		
		for (int x=0; x<structNodes.getLength(); x++) {
			Element struct = (Element) structNodes.item(x);
			String structName = struct.getAttribute("name");
			if (programNodes) {
				structName = '[' + structName + ']';
			}
			loadSinglePojoStruct(struct, structName);
		}
	}

	private void loadSinglePojoStruct(Element struct, String structName) {
		List<PcmlElement> pcmlList = pcmlStructMap.get(structName);
		if (pcmlList!=null) {
			NodeList dataNodes = struct.getChildNodes();
			// iterate structure elements
			for (int y=0; y<dataNodes.getLength(); y++) {
				Node node = dataNodes.item(y);
				if (node!=null && node instanceof Element) {
					Element data = (Element) node;
					PojoElement pojoElement = getPojoElement(data);
					PcmlElement pcmlElement = findElement(pojoElement.getPcmlName(), pcmlList);
					if (pcmlElement!=null) {
						pcmlElement.setPojoElement(pojoElement);
					} else {
						logger.warn("Element " + structName + '.' + 
								pojoElement.getPcmlName() + 
								" exists in the Pojo map but does not" +
								" exist in the PCML");
					}
				} // if (node!=null && node instanceof Element)
			} // for (int y=0; y<dataNodes.getLength(); y++) 
		} else {
			// pcmlList == null
			logger.warn("Structure " + structName + 
					" exists in the Pojo map but does not exist in the PCML");
		}
	}

	private PcmlElement findElement(String pcmlName, List<PcmlElement> pcmlList) {
		// linear search
		for (PcmlElement pe : pcmlList) {
			if (pcmlName.equals(pe.getName())) {
				return pe;
			}
		}
		return null;
	}

	private PojoElement getPojoElement(Element data) {
		PojoElement pe = new PojoElement();
		pe.setPcmlName(data.getAttribute("pcmlName"));
		pe.setPojoFieldName(data.getAttribute("pojoFieldName"));
		pe.setConvertToPojo(data.getAttribute("convertToPojo"));
		pe.setPojoFormat(data.getAttribute("pojoFormat"));
		pe.setConvertToLegacy(data.getAttribute("convertToLegacy"));
		pe.setLegacyFormat(data.getAttribute("legacyFormat"));
		pe.setPojoFieldType(reflectionHelper.getType(pe.getPojoFieldName()));
		return pe;
	}
	
	private static Document getDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true); 
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		return builder.parse(is);
	}

}
