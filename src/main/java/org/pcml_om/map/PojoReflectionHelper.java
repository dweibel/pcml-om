package org.pcml_om.map;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class PojoReflectionHelper {
	
	private static Logger logger = Logger.getLogger(PojoReflectionHelper.class.getName());
	
	private Map<String, PojoReflectionClass> classMap = new HashMap<String, PojoReflectionHelper.PojoReflectionClass>();
	
	public Class<?> getType (String classField) {
		Class<?> result = null;
		String classname = PojoElement.getClassName(classField);
		String fieldname = PojoElement.getFieldName(classField);
		if (classname==null) return null;
		if (fieldname==null || fieldname.length()<1) return null;

		try {
			PojoReflectionClass prc = classMap.get(classname);
			if (prc==null) {
				prc = new PojoReflectionClass(classname);
				classMap.put(classname, prc);
			}
			result = prc.getFieldType(fieldname);
			if (result == null) {
				logger.error(classname + " does not contain field named " + fieldname + '.');
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to load class " + classname, e);
		}
		return result;
	}
	
	protected class PojoReflectionClass {
		Class<?> c;
		Field fieldList[];
		Method methodList[];
		
		PojoReflectionClass(String className) throws ClassNotFoundException {
			c = Class.forName(className);
			fieldList = c.getDeclaredFields();
			methodList = c.getMethods();
		}
		
		Class<?> getFieldType(String fieldname) {
			for (Field field : fieldList) {
				if (fieldname.equals(field.getName())) {
					return field.getType();
				}
			}
			return null;
		}
		
	} // protected class PojoReflectionClass

	public static Object getFieldValue(Object targetObj, String fieldname) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "get" + capField(fieldname);
		Method getterMethod = targetObj.getClass().getMethod(methodName);
		return getterMethod.invoke(targetObj);
	}

	/*
	public static void setFieldValue(Object targetObj, String fieldname, Object val) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "set" + capField(fieldname);
		Method setterMethod = targetObj.getClass().getMethod(methodName, val.getClass());
		setterMethod.invoke(targetObj, val);
	}
	*/
	
	public static void setFieldValue(Object targetObj, String fieldname, Object val, Class<?> valType) 
			throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String methodName = "set" + capField(fieldname);
		Method setterMethod = targetObj.getClass().getMethod(methodName, valType);
		setterMethod.invoke(targetObj, val);
	}
	
	private static String capField(String fieldname) {
		String startChar = fieldname.substring(0, 1).toUpperCase();
		return startChar + fieldname.substring(1);
	}
	
	public static Object invokePojoConversion(String classname, String methodname, Object value) 
			throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> convertClass = Class.forName(classname);
		Method convertMethod = convertClass.getMethod(methodname, value.getClass());
		return convertMethod.invoke(convertClass, value);
	}
	
/* Experiments in type reflections
	
	public static void main(String args[]) {
		try {
			PojoReflectionHelper prh = new PojoReflectionHelper();
			PojoReflectionClass prc = prh.new PojoReflectionClass(TestClass.class.getName());
			for (Field prcField: prc.fieldList) {
				System.out.println(prcField.getName() + ": " + prcField.getType().getName());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	protected class TestClass {
		public byte scalarByte;
		public Byte objByte;
		public byte byteArray[];
		public short scalarShort;
		public Short objShort;		
		public int scalarInt;
		public Integer objInt;
		public int intArray[];
		public long scalarLong;
		public Long objLong;
		public float scalarFloat;
		public float floatArray[];
		public double scalarDouble;
		public Double objDouble;
		
		public String string;
		public String stringArray[];
	}

	results:
		scalarByte: byte
		objByte: java.lang.Byte
		byteArray: [B
		scalarShort: short
		objShort: java.lang.Short
		scalarInt: int
		objInt: java.lang.Integer
		intArray: [I
		scalarLong: long
		objLong: java.lang.Long
		scalarFloat: float
		floatArray: [F
		scalarDouble: double
		objDouble: java.lang.Double
		string: java.lang.String
		stringArray: [Ljava.lang.String;
	
*/
	
} // class PojoReflectionHelper 
