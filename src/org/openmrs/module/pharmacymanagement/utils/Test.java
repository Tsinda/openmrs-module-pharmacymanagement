package org.openmrs.module.pharmacymanagement.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.module.pharmacymanagement.DrugProduct;

public class Test {

	/**
	 * Auto generated method comment
	 * 
	 * @param args
	 * @throws ParseException
	 */

	public static HashMap<String, DrugProduct> sortHashMap(HashMap<String, DrugProduct> input){
	    Map<String, DrugProduct> tempMap = new HashMap<String, DrugProduct>();
	    for (String wsState : input.keySet()){
	        tempMap.put(wsState,input.get(wsState));
	    }

	    List<String> mapKeys = new ArrayList<String>(tempMap.keySet());
	    List<DrugProduct> mapValues = new ArrayList<DrugProduct>(tempMap.values());
	    HashMap<String, DrugProduct> sortedMap = new LinkedHashMap<String, DrugProduct>();
	    TreeSet<DrugProduct> sortedSet = new TreeSet<DrugProduct>(mapValues);
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;
	    for (int i=0; i<size; i++){
	        sortedMap.put(mapKeys.get(mapValues.indexOf(sortedArray[i])), 
	                      (DrugProduct)sortedArray[i]);
	    }
	    return sortedMap;
	}
	
	public static void main(String[] args) {

		HashMap<String, Double> unSorted = new HashMap<String, Double>();
		unSorted.put("Bristol", 23.45);
		unSorted.put("London", 345.122);
		unSorted.put("Manchester", 12.3);
		unSorted.put("Edinburgh", 11.4); 
		
		HashMap<String, Double> sorted = new HashMap<String, Double>();
//		sorted = sortHashMap(unSorted);
		
		for (String cityName : sorted.keySet()){
		    System.out.println(cityName + " " + sorted.get(cityName));
		}
			
	}


}
