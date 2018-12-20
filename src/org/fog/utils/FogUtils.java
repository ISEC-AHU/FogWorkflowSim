package org.fog.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FogUtils {
	private static int ENTITY_ID = 1;
	
	public static String getSensorTypeFromSensorName(String sensorName){
		return sensorName.substring(sensorName.indexOf('-')+1, sensorName.lastIndexOf('-'));
	}
	
	public static int generateEntityId(){
		return ENTITY_ID++;
	}
	
	public static int USER_ID = 1;
	
	public static int MAX = 10000000;
	
	public static Map<String, GeoCoverage> appIdToGeoCoverageMap = new HashMap<String, GeoCoverage>();
	
	public static void set1(){
		ENTITY_ID = 1;
	}
}
