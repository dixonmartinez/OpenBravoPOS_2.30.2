/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openbravo.format;

/**
 *
 * @author adrian
 */
public class DoubleUtils {

	/**
	 * Fix Decimals
	 * Add parameter boolean round verify if is necesary round amount
	 * @param value
	 * @param round
	 * @return
	 */
    public static double fixDecimals(Number value, boolean round) {
    	if(round) {
    		return Math.round(Math.rint((value).doubleValue() * 1000000.0) / 1000000.0);
    	}else {    		
    		return Math.rint((value).doubleValue() * 1000000.0) / 1000000.0;
    	}
    }
    
}
