package org.pcml_om;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.pcml_om.map.MapFactory;
import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PcmlPojoMap;
import org.pcml_om.map.PojoElement;
import org.pcml_om.map.PojoReflectionHelper;
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
			pojoConverter = new PojoConverter(pcmlPojoMap.getPcmlDefaultDateFormat());
			pcmlConverter = new PcmlConverter(pcmlPojoMap.getPcmlDefaultDateFormat());
		} catch (Exception e) {
			throw new PcmlCallException("Unable to create PcmlDao", e);
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

		if (programName==null || programName.length() < 1) {
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
			if (!pcmlElement.isOutputOnly()) {
				if (pcmlElement.getPojoElement() == null) {
					initNullElement(pcmlElement, pcDoc);
				} else {
					initElement(pcmlElement, pcDoc, objectMap);
				}
			}
		}
	}

	private void initNullElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc) throws PcmlException {
		if (pcmlElement.getCount() > 1) {
			initRepeatingNullElement(pcmlElement, pcDoc);
		} else {
			String name = pcmlElement.getQualifiedName();
			if (pcmlElement.getType().equals(PcmlElement.CHAR)) {
				pcDoc.setStringValue(name, "", BidiStringType.DEFAULT);
			} else if (pcmlElement.getType().equals(PcmlElement.BYTE)) {
				pcDoc.setValue(name, new byte[0]);
			} else {
				pcDoc.setValue(name, new BigDecimal(0));
			}
		}
	}

	private void initRepeatingNullElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc) throws PcmlException {
		int count = pcmlElement.getCount();
		List<PcmlElement> subElements = pcmlElement.getPcmlSubstruct();
		int[] indices = new int[1];
		for (int i = 0; i < count; i++) {
			indices[0] = i;
			for(PcmlElement subElement : subElements) {
				initNullSubElement(subElement, indices, pcDoc);
			}
		}
	}

	private void initNullSubElement(PcmlElement subElement, int[] indices,
			ProgramCallDocument pcDoc) throws PcmlException {
		String name = subElement.getQualifiedName();
		if (subElement.getType().equals(PcmlElement.CHAR)) {
			pcDoc.setStringValue(name, indices, "", BidiStringType.DEFAULT);
		} else if (subElement.getType().equals(PcmlElement.BYTE)) {
			pcDoc.setValue(name, indices, new byte[0]);
		} else {
			pcDoc.setValue(name, indices, new BigDecimal(0));
		}
	}

	private void initElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc, Map<String, Object> objectMap)
			throws PcmlException, PcmlCallException {
		if (pcmlElement.getCount() > 1) {
			initRepeatingElement(pcmlElement, pcDoc, objectMap);
		} else {
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
				pcDoc.setValue(pcmlElement.getQualifiedName(), pojoVal);
			}
		}
	}
	
	private void initRepeatingElement(PcmlElement pcmlElement,
			ProgramCallDocument pcDoc, Map<String, Object> objectMap)
			throws PcmlException, PcmlCallException {
		// The repeating element must be mapped to an POJO array
		// retrieve the array of repeating elements
		PojoElement pojoContainerElement = pcmlElement.getPojoElement();
		String fieldname = pojoContainerElement.getFieldName();
		String classname = pojoContainerElement.getClassName();
		Object targetContainerObj = objectMap.get(classname);
		if (targetContainerObj == null) {
			throw new PcmlCallException("Missing input class: " + classname);
		}
		Object container;
		try {
			container = PojoReflectionHelper.getFieldValue(targetContainerObj, fieldname);
		} catch (Exception e) {
			throw new PcmlCallException("Unable to retrieve field " + 
					fieldname + " from class " + classname, e);
		}
		if (container == null) {
			initRepeatingNullElement(pcmlElement, pcDoc);
			return;
		}
		Object[] containerArray = null;
		try {
			containerArray = (Object[]) container;
		} catch (ClassCastException cce) {
			throw new PcmlCallException("Repeating element " + pcmlElement.getQualifiedName() + 
					" is not mapped to a POJO array");
		}
		
		// iterate the repeating elements
		int count = pcmlElement.getCount();
		List<PcmlElement> subElementList = pcmlElement.getPcmlSubstruct();
		PcmlElement[] subElements = new PcmlElement[subElementList.size()];
		subElements = subElementList.toArray(subElements);
		int[] indices = new int[1];
		for (int i = 0; i < count; i++) {
			indices[0] = i;
			for(PcmlElement subElement : subElements) {
				PojoElement pojoSubElement = subElement.getPojoElement();
				Object targetElementObj = getElementObj(containerArray, i);
				if (targetElementObj == null) {
					initNullSubElement(subElement, indices, pcDoc);
				} else {
					Object pojoVal = pojoConverter.get(subElement, targetElementObj);
					if (pojoVal == null) {
						initNullSubElement(subElement, indices, pcDoc);
						logger.warn("Unable to retrieve value for "
								+ pojoSubElement.getPojoFieldName());
					} else {
						pcDoc.setValue(subElement.getQualifiedName(), indices, pojoVal);
					}
				}
			}
		}
	}


	private Object getElementObj(Object[] containerArray, int i) {
		if (containerArray==null || containerArray.length <= i) {
			return null;
		}
		return containerArray[i];
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
			throws PcmlException, PcmlCallException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
		for (PcmlElement pcmlElement : pcmlElements) {
			if (pcmlElement.getCount() > 1) {
				setOutputRepeating(pcmlElement, objectMap, pcDoc);
			} else {
				PojoElement pojoElement = pcmlElement.getPojoElement();
				if (pojoElement != null) {
					String classname = pojoElement.getClassName();
					Object pojoDao = objectMap.get(classname);
					if (pojoDao != null) {
						Object pcmlVal = pcDoc.getValue(pcmlElement.getQualifiedName());
						pcmlConverter.get(pcmlElement, pcmlVal, pojoDao);
					}
				}
			}
		}
	}

	private void setOutputRepeating(PcmlElement pcmlElement,
			Map<String, Object> objectMap, ProgramCallDocument pcDoc) 
			throws PcmlException, PcmlCallException, SecurityException, IllegalArgumentException, NoSuchMethodException, 
			IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException {
		PojoElement pojoElement = pcmlElement.getPojoElement();
		if (pojoElement != null) {
			// initialize array of sub-elements
			List<PcmlElement> subElementList = pcmlElement.getPcmlSubstruct();
			PcmlElement[] subElements = new PcmlElement[subElementList.size()];
			subElements = subElementList.toArray(subElements);
			
			List<Object> outputList = new ArrayList<Object>();
			int maxCount = pcmlElement.getCount();
			int[] indices = new int[1];
			boolean finished = false;
			for (int i = 0; i < maxCount && !finished; i++) {
				indices[0] = i;
				Object pojoOut = null;
				boolean pojoValid = false;
				for(PcmlElement subElement : subElements) {
					PojoElement pojoSubElement = subElement.getPojoElement();
					if (pojoSubElement != null) {
						// create new output item, if necessary
						if (pojoOut == null) {
							Class<?> pojoOutType = Class.forName(pojoSubElement.getClassName());
							pojoOut = pojoOutType.newInstance();
						}
						String testVal = pcDoc.getStringValue(subElement.getQualifiedName(), indices, BidiStringType.DEFAULT);
						if (testVal != null && testVal.length() > 0) {
							pojoValid = true;
							Object pcmlVal = pcDoc.getValue(subElement.getQualifiedName(), indices);
							pcmlConverter.get(subElement, pcmlVal, pojoOut);
						}
					}
				}
				if (pojoValid) {
					outputList.add(pojoOut);
				} else {
					finished = true;
				}
			}			
			// set the resulting array into the output POJO
			Object[] containerArray = outputList.toArray();
			Object pojoDao = objectMap.get(pojoElement.getClassName());
			PojoReflectionHelper.setFieldValue(
									pojoDao, 
									pojoElement.getFieldName(), 
									containerArray, 
									pojoElement.getPojoFieldType() );
		}
	}

}
