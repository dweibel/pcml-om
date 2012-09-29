package org.pcml_om.util;

public class LoggingUtil {
	
	public static String getDuration(long startTime) {
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		return String.format("%d ms", duration);
	}
	
	
}
