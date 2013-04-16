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

		Object str = null;
		int in = 0;
		if(str != null) {
			in = Integer.valueOf(str + "");
			System.out.println(in);
		} else {
			System.out.println("not null");
		}
		
			
	}

//	private static Comparator<Map.Entry<K, V>> comparator = new Comparator<Map.Entry<K, V>>() {
//		public int compare(Map.Entry<K, V> obj1, Map.Entry<K, V> obj2) {
//			// do comparing on values first, keys second
//			return obj1.getValue().toLowerCase()
//					.compareTo(obj2.getValue().toLowerCase());
//		}
//	};

}
