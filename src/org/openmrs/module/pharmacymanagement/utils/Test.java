package org.openmrs.module.pharmacymanagement.utils;

import java.text.ParseException;

public class Test {
	

	/**
	 * Auto generated method comment
	 * 
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		
		String cn = "19/09/2012,;1,Urine,guess,19/09/2012,;2,Hemoglobin,guess,19/09/2012,;20/09/2012,;2,CD4,guess,20/09/2012,;";
		String[] arr = cn.split(";");
		for(String str : arr) {
			String[] arr1 = str.split(",");
			for(int i = 0; i < arr1.length; i++) {
				System.out.print(arr1[i]);
				if(arr1.length != (i+1)) {
					System.out.print(", ");
				}				
			}
			System.out.println("");
		}
	}

}
