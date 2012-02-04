package test.pcml_om.map;

import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.pcml_om.map.PojoReflectionHelper;

public class TestPojoReflectionHelper {
	
	String testClassname = TestClass.class.getName();
	String testField = "myField";
	String testClassField = testClassname + '.' + testField;
	String testConversionMethod = "convertField";
	PojoReflectionHelper prh = new PojoReflectionHelper();
	TestClass testClassInstance = new TestClass();

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetType() {
		Object type = prh.getType(testClassField);
		assert(type instanceof String);
	}

	@Test
	public void testGetter() 
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String testValue = "Test 123";
		testClassInstance.setMyField(testValue);
		Object val = PojoReflectionHelper.getFieldValue(testClassInstance, testField);
		assert(testValue.equals(val.toString()));
	}

	@Test
	public void testSetter() 
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String testValue = "Test 456";
		PojoReflectionHelper.setFieldValue(testClassInstance, testField, testValue, String.class);
		String resultValue = testClassInstance.getMyField();
		assert(testValue.equals(resultValue));
	}

	@Test
	public void testInvokePojoConversion() 
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		String testValue = "Test 789";
		String convertedValue = TestClass.convertField(testValue);
		Object val = PojoReflectionHelper.invokePojoConversion(testClassname, "convertField", testValue);
		assert(convertedValue.equals(val.toString()));
	}
	
	public static class TestClass {
		private String myField = null;
		public void setMyField(String myField) { this.myField = myField; }
		public String getMyField() { return myField; }
		public static String convertField(String input) { return "converted " + input; } 
	}



}
