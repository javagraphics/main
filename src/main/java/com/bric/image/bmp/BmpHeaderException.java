/*
 * @(#)BmpHeaderException.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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
package com.bric.image.bmp;

import java.io.IOException;

/** Thrown when an input stream does not begin with "BM",
 * which signals this isn't a valid BMP image.
 *
 */
public class BmpHeaderException extends IOException {
	private static final long serialVersionUID = 1L;

	public BmpHeaderException() {
		super();
	}

	public BmpHeaderException(String s) {
		super(s);
	}
}
