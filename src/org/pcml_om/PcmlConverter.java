package org.pcml_om;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PojoElement;
import org.pcml_om.map.PojoReflectionHelper;

public class PcmlConverter extends Converter{
	private String defaultDateFormat;
	
	public PcmlConverter(String defaultDateFormat) {
		super(defaultDateFormat);
	}
	
	public Object get(PcmlElement pcmlElement, Object pcmlVal, Object pojoDao)
			throws PcmlCallException {
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String pojoTypeStr = pojoElement.getPojoFieldType().toString();
		if (valid(pojoElement.getConvertToLegacy())) {
			convertPcmlValExplicit(pojoElement, pcmlVal, pojoDao);
		} else if (pojoTypeStr.equals("class java.util.Date")) {
			convertPcmlDateVal(pojoElement, pcmlVal, pojoDao);
		} else if (pojoTypeStr.equals("class java.lang.String")) {
			convertPcmlStringVal(pojoElement, pcmlVal, pojoDao);
		} else if (pojoTypeStr.equals("[B") && pcmlElement.getType().equals(PcmlElement.BYTE)) {
			setVal(pojoElement, pojoDao, pcmlVal);
		} else {
			convertPcmlNumericVal(pojoElement, pcmlVal, pojoDao);
		}
		return null;
	}
	
	private void convertPcmlValExplicit(PojoElement pojoElement, Object pcmlVal, Object pojoDao) throws PcmlCallException {
		Object pojoVal = null;
		String classMethod = pojoElement.getConvertToPojo();
		String classname = PojoElement.getClassName(classMethod);
		String methodname = PojoElement.getFieldName(classMethod);
		try {
			pojoVal = PojoReflectionHelper.invokePojoConversion(classname, methodname, pcmlVal);
		} catch (Exception e) {
			throw new PcmlCallException("Error accessing conversion method for " +classMethod +
					".  This method must be declared static", e);
		}
		setVal(pojoElement, pojoDao, pojoVal);
	}

	private void convertPcmlDateVal(PojoElement pojoElement, Object pcmlVal, Object pojoDao) throws PcmlCallException {
		String dateFmt = pojoElement.getPojoFormat();
		if (!valid(dateFmt)) {
			dateFmt = defaultDateFormat;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFmt);
		Date pojoDate;
		try {
			pojoDate = sdf.parse(pcmlVal.toString());
		} catch (ParseException e) {
			throw new PcmlCallException("Unable to parse AS400 value to date: " +
					pojoElement.getPcmlName() + " = " + pcmlVal.toString() +
					", date format = " + dateFmt, e);
		}
		setVal(pojoElement, pojoDao, pojoDate);
	}

	private void convertPcmlStringVal(PojoElement pojoElement, Object pcmlVal, Object pojoDao) throws PcmlCallException {
		String pojoVal = pcmlVal.toString();
		setVal(pojoElement, pojoDao, pojoVal);
	}
	

	private void convertPcmlNumericVal(PojoElement pojoElement, Object pcmlVal, Object pojoDao) throws PcmlCallException {
		String pcmlStringVal = pcmlVal.toString();
		if (!isNumeric(pcmlStringVal)) {
			throw new PcmlCallException("PCML value " + pojoElement.getPcmlName() + 
						" contains invalid numeric data: " + pcmlStringVal);
		}
		String pojoFieldType = pojoElement.getPojoFieldType().toString();
		Object pojoVal = null;
		if (pojoFieldType.equals("byte")) {
			pojoVal = Byte.parseByte(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Byte")) {
			pojoVal = new Byte(pcmlStringVal);
		} else if (pojoFieldType.equals("short")) {
			pojoVal = Short.parseShort(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Short")) {
			pojoVal = new Short(pcmlStringVal);
		} else if (pojoFieldType.equals("int")) { 
			pojoVal = Integer.parseInt(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Integer")) {
			pojoVal = new Integer(pcmlStringVal);
		} else if (pojoFieldType.equals("long")) {
			pojoVal = Long.parseLong(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Long")) {
			pojoVal = new Long(pcmlStringVal);
		} else if (pojoFieldType.equals("float")) {
			pojoVal = Float.parseFloat(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Float")) {
			pojoVal = new Float(pcmlStringVal);
		} else if (pojoFieldType.equals("double")) {
			pojoVal = Double.parseDouble(pcmlStringVal);
		} else if (pojoFieldType.equals("class java.lang.Double")) {
			pojoVal = new Double(pcmlStringVal);
		}
		if (pojoVal == null) {
			throw new PcmlCallException("Pojo field " + 
					pojoElement.getPojoFieldName() + 
					" has an invalid type for an implicit conversion: " + 
					pojoFieldType);
		}
		setVal(pojoElement, pojoDao, pojoVal);
	}
	
	private void setVal(PojoElement pojoElement, Object targetObj, Object val) throws PcmlCallException {
		String classField = pojoElement.getPojoFieldName();
		String fieldname = PojoElement.getFieldName(classField);
		setVal(targetObj, fieldname, val, pojoElement.getPojoFieldType());
	}

	private void setVal(Object targetObj, String fieldname, Object val, Class<?> valType) throws PcmlCallException {
		try {
			PojoReflectionHelper.setFieldValue(targetObj, fieldname, val, valType);
		} catch (Exception e) {
			throw new PcmlCallException("Error accessing setter method for " +targetObj.getClass().getName());
		}
	}
	
	private boolean isNumeric(String s) {  
        return s.matches("\\d+");  
    }  	
}
