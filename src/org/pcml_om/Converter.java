package org.pcml_om;

public abstract class Converter {
	protected String defaultDateFormat;
	
	public Converter(String defaultDateFormat) {
		this.defaultDateFormat = defaultDateFormat;
	}

	public static boolean valid(String testStr) {
		return (testStr!=null && testStr.length()> 0);
	}
	

}
