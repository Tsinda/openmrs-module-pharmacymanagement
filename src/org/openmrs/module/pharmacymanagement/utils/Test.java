package org.openmrs.module.pharmacymanagement.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.openmrs.Concept;
import org.openmrs.Drug;

public class Test {

	/**
	 * Auto generated method comment
	 * 
	 * @param args
	 * @throws ParseException
	 */

	// public int compare(Map.Entry<K,V> entry1, Map.Entry<K,V> entry2)
	// {
	// // assuming there are no null values
	// return entry1.getValue().compareTo(entry2.getValue());
	// }
	//
	// public int compare(Map.Entry<K,V> entry1, Map.Entry<K,V> entry2)
	// {
	// // assuming there are no null keys or values
	// int diff = entry1.getValue().compareTo(entry2.getValue());
	// if (diff == 0)
	// {
	// diff = entry1.getKey().compareTo(entry2.getKey());
	// }
	// return diff;
	// }
	//
	public static void main(String[] args) {

		Map<Integer, String> map = new HashMap<Integer, String>();

		map.put(2, "oranges");
		map.put(1, "pears");
		map.put(3, "three");
		map.put(4, "bananas");
		map.put(8, "bananas");
		map.put(3, "crayons");

//		SortedMap<Map.Entry<K, V>> entries = new TreeMap<Map.Entry<K, V>>(
//				comparator);
//		entries.addAll(map.entrySet());
//		
//		for(Map.Entry<Integer, String> : entries)
			
	}

//	private static Comparator<Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
//		public int compare(Map.Entry<K, V> obj1, Map.Entry<K, V> obj2) {
//			// do comparing on values first, keys second
//			return obj1.getValue().toLowerCase()
//					.compareTo(obj2.getValue().toLowerCase());
//		}
//	};

}
