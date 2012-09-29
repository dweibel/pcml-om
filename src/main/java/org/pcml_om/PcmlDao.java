package org.pcml_om;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pcml_om.map.MapFactory;
import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PcmlPojoMap;
import org.pcml_om.map.PojoElement;
import org.pcml_om.util.LoggingUtil;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.BidiStringType;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;

public class PcmlDao {

	private static Logger logger = Logger.getLogger(PcmlDao.class.getName());

	private PcmlPojoMap pcmlPojoMap;
	private PojoConverter pojoConverter;
	private PcmlConverter pcmlConverter;
	private String pcmlDocName;
	private ClassLoader pcmlClassLoader;

	/**
	 * Construct the PcmlDao object.  
	 * @param pcmlDocName PCML document name without the .pcml extention
	 * @param pcmlClassLoader ClassLoader for the PCML document
	 * @param pojoDocName PCML-OM configuration file without the .xml extension
	 * @param pojoClassLoader ClassLoader for the PCML-OM configuration file
	 * @throws PcmlCallException when files are not found or errors occur reading the files
	 */
	public PcmlDao(String pcmlDocName, ClassLoader pcmlClassLoader,
			String pojoDocName, ClassLoader pojoClassLoader)
			throws PcmlCallException {
		this.pcmlDocName = pcmlDocName;
		this.pcmlClassLoader = pcmlClassLoader;
		InputStream pcmlIs = pcmlClassLoader.getResourceAsStream(pcmlDocName + ".pcml");
		InputStream pojoIs = pojoClassLoader.getResourceAsStream(pojoDocName + ".xml");
		if (pcmlIs==null) {
			throw new PcmlCallException("Unable to find resource " + pcmlDocName + ".pcml");
		} else if (pojoIs==null) {
			throw new PcmlCallException("Unable to find resource " + pojoDocName + ".xml");
		}
		try {
			pcmlPojoMap = MapFactory.getPcmlPojoMap(pcmlIs, pojoIs);
			pojoConverter = new PojoConverter(pcmlPojoMap.getDefaultDateFormat());
			pcmlConverter = new PcmlConverter(pcmlPojoMap.getDefaultDateFormat());
		} catch (Exception e) {
			throw new PcmlCallException("Unable to create As400Dao", e);
		}
	}

	/**
	 * Call an AS400 program
	 * @param as400Connection
	 * @param programName
	 * @param args
	 * @throws PcmlCallException
	 */
	public void callAs400(AS400 as400Connection, String programName,
			Object... args) throws PcmlCallException {
		if (args == null) {
			throw new PcmlCallException("No Pojo object parameters");
		}

		if (!Converter.valid(programName)) {
			throw new PcmlCallException("No program name");
		}

		// place pojo objects into map keyed by class name
		Map<String, Object> objectMap = new HashMap<String, Object>();
		for (Object obj : args) {
			objectMap.put(obj.getClass().getName(), obj);
		}

		ProgramCallDocument pcDoc = getCallDocument(as400Connection);
		PcmlElement[] pcmlElements = pcmlPojoMap.getPcmlElements(programName);
		try {
			setInputParameters(pcmlElements, objectMap, pcDoc);
		} catch (PcmlCallException ace) {
			throw ace;
		} catch (Exception e) {
			throw new PcmlCallException("Unable to set input parameters", e);
		}

		call(as400Connection, pcDoc, programName);

		try {
			setOutputParameters(pcmlElements, objectMap, pcDoc);
		} catch (Exception e) {
			if (e instanceof PcmlCallException) {
				throw (PcmlCallException) e;
			}
			throw new PcmlCallException("Unable to set output parameters", e);
		}
	}

	public ProgramCallDocument getCallDocument(AS400 as400)
			throws PcmlCallException {
		try {
			return new ProgramCallDocument(as400, pcmlDocName, pcmlClassLoader);
		} catch (PcmlException pe) {
			throw new PcmlCallException("Unable to create ProgramCallDocument",
					pe);
		}
	}

	private void setInputParameters(PcmlElement[] pcmlElements,
			Map<String, Object> objectMap, ProgramCallDocument pcDoc)
			throws PcmlException, PcmlCallException {
		for (PcmlElement pcmlElement : pcmlElements) {
			if (pcmlElement.getPojoElement() == null) {
				initNullElement(pcmlElement, pcDoc);
			} else {
				initElement(pcmlElement, pcDoc, objectMap);
			}
		}
	}

	private void initNullElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc) throws PcmlException {
		String name = pcmlElement.getName();
		if (pcmlElement.getType() == PcmlElement.CHAR) {
			pcDoc.setStringValue(name, "", BidiStringType.DEFAULT);
		} else if (pcmlElement.getType() == PcmlElement.BYTE) {
			pcDoc.setValue(name, new byte[0]);
		} else {
			pcDoc.setValue(name, new BigDecimal(0));
		}
	}

	private void initElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc, Map<String, Object> objectMap)
			throws PcmlException, PcmlCallException {
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String classField = pojoElement.getPojoFieldName();
		String classname = PojoElement.getClassName(classField);
		Object targetObj = objectMap.get(classname);
		if (targetObj == null) {
			throw new PcmlCallException("Missing input class: " + classname);
		}
		Object pojoVal = pojoConverter.get(pcmlElement, targetObj);
		if (pojoVal == null) {
			initNullElement(pcmlElement, pcDoc);
			logger.warn("Unable to retrieve value for "
					+ pojoElement.getPojoFieldName());
		} else {
			pcDoc.setValue(pcmlElement.getName(), pojoVal);
		}
	}

	private void call(AS400 as400, ProgramCallDocument pcDoc, String programName)
			throws PcmlCallException {
		try {
			long startTime = System.currentTimeMillis();
			boolean success = pcDoc.callProgram(programName);
			logger.debug("Call to " + programName + " took "
					+ LoggingUtil.getDuration(startTime));
			if (!success) {
				final String NEWLINE = String.format("%n");
				StringBuffer errors = new StringBuffer();
				errors.append("Errors calling " + programName + ':' + NEWLINE);
				AS400Message[] msgs = pcDoc.getMessageList(programName);
				for (AS400Message msg : msgs) {
					errors.append("    " + msg.getID() + ": " + msg.getText()
							+ NEWLINE);
				}
				throw new PcmlCallException(errors.toString());
			}
		} catch (PcmlException e) {
			throw new PcmlCallException("Exception calling " + programName, e);
		}
	}

	private void setOutputParameters(PcmlElement[] pcmlElements,
			Map<String, Object> objectMap, ProgramCallDocument pcDoc)
			throws PcmlException, PcmlCallException {
		for (PcmlElement pcmlElement : pcmlElements) {
			PojoElement pojoElement = pcmlElement.getPojoElement();
			if (pojoElement != null) {
				String classField = pojoElement.getPojoFieldName();
				String classname = PojoElement.getClassName(classField);
				Object pojoDao = objectMap.get(classname);
				if (pojoDao != null) {
					Object pcmlVal = pcDoc.getValue(pcmlElement.getName());
					pcmlConverter.get(pcmlElement, pcmlVal, pojoDao);
				}
			}
		}
	}

}
