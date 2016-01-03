/*
 * @(#)FEEConstants.java
 *
 * $Date: 2014-11-27 07:55:25 +0100 (Do, 27 Nov 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.math.symbolic;

/** Constants related to defining a formula for fractional exponent elimination. */
public interface FEEConstants {
	
	/** The variable to use in the formula. */
	public final static String K = "K";
	
	/** A series of constants, where the nth term should related to K^n. */
	public final static String[] C = new String[] {
		"C0", 
		"C1", 
		"C2", 
		"C3", 
		"C4", 
		"C5", 
		"C6", 
		"C7", 
		"C8"};
}
