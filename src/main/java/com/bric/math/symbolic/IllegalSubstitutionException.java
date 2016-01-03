/*
 * @(#)IllegalSubstitutionException.java
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

/** This exception indicates a substitution couldn't be applied.
 */
public class IllegalSubstitutionException extends Exception {
	private static final long serialVersionUID = 1L;

	public IllegalSubstitutionException() {}
	
	public IllegalSubstitutionException(String s) {
		super(s);
	}
	
	public IllegalSubstitutionException(Exception e) {
		super(e);
	}
}
