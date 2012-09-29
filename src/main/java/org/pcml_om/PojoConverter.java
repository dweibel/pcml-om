package org.pcml_om;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PojoElement;
import org.pcml_om.map.PojoReflectionHelper;

public class PojoConverter extends Converter {

	public PojoConverter(String defaultDateFormat) {
		super(defaultDateFormat);
	}


	public Object get(PcmlElement pcmlElement, Object pojoObject)
			throws PcmlCallException {
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String pojoTypeStr = pojoElement.getPojoFieldType().toString();
		Object pojoVal = getVal(pcmlElement, pojoObject);
		Object pojoResultVal = null;
		if (valid(pojoElement.getConvertToLegacy())) {
			pojoResultVal = convertPojoValExplicit(pcmlElement, pojoVal);
		} else if (pojoTypeStr.equals("class java.util.Date")) {
			pojoResultVal = convertPojoDateVal(pcmlElement, (Date)pojoVal);
		} else if (pojoTypeStr.equals("class java.lang.String")) {
			pojoResultVal = convertPojoStringVal(pcmlElement, (String)pojoVal);
		} else if (pojoTypeStr.equals("[B")) {
			// no implicit conversions for byte arrays
			pojoResultVal = getVal(pcmlElement, pojoObject);
		} else {
			// if it meets no other conversion criteria, it must be numeric
			pojoResultVal = convertPojoNumericVal(pcmlElement, pojoVal);
		}
		return pojoResultVal;
	}


	private Object convertPojoValExplicit(PcmlElement pcmlElement, Object pojoVal) throws PcmlCallException {
		Object val = null;
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String classMethod = pojoElement.getConvertToLegacy();
		String classname = PojoElement.getClassName(classMethod);
		String methodname = PojoElement.getFieldName(classMethod);
		try {
			val = PojoReflectionHelper.invokePojoConversion(classname, methodname, pojoVal);
		} catch (Exception e) {
			throw new PcmlCallException("Error accessing conversion method for " +classMethod +
					".  This method must be declared static");
		}
		return val;
	}

	private Object convertPojoDateVal(PcmlElement pcmlElement, Date pojoVal) {
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String dateFmt = pojoElement.getPojoFormat();
		if (!valid(dateFmt)) {
			dateFmt = defaultDateFormat;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(dateFmt);
		String resultDate = sdf.format(pojoVal);
		if (pcmlElement.getType().equals(PcmlElement.CHAR)) {
			return resultDate;
		} else {
			return new BigDecimal(resultDate);
		}
	}

	
	private Object convertPojoStringVal(PcmlElement pcmlElement, String pojoVal) {
		if (pcmlElement.getType().equals(PcmlElement.CHAR)) {
			return pojoVal;
		} else {
			return new BigDecimal(pojoVal);
		}
	}

	private Object convertPojoNumericVal(PcmlElement pcmlElement, Object pojoVal) {
		BigDecimal bdVal;
		if (pojoVal instanceof BigDecimal){
			bdVal = (BigDecimal)pojoVal;
		} else {
			bdVal = new BigDecimal(pojoVal.toString());
		}
		Object result = bdVal;
		if (pcmlElement.getType().equals(PcmlElement.CHAR)) {
			String format = pcmlElement.getPojoElement().getPojoFormat();
			if (valid(format)) {
				MessageFormat mf = new MessageFormat(format);
				result = mf.format(pojoVal);
			} else {
				result = bdVal.toString();
			}
		}
		return result;
	}

	private Object getVal(PcmlElement pcmlElement, Object targetObj) throws PcmlCallException {
		Object val = null;
		PojoElement pojoElement = pcmlElement.getPojoElement();
		String classField = pojoElement.getPojoFieldName();
		String fieldname = PojoElement.getFieldName(classField);
		try {
			val = PojoReflectionHelper.getFieldValue(targetObj, fieldname);
		} catch (Exception e) {
			throw new PcmlCallException("Error accessing getter method for " +classField);
		}
		return val;
	}
}
