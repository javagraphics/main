/*
 * @(#)BmpTests.java
 *
 * $Date: 2015-03-03 04:58:22 +0100 (Di, 03 Mär 2015) $
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

import junit.framework.TestCase;

/** This is a developer's tool to confirm that BMPs are being read
 * correctly, and to run some comparison tests.
 *
 */
public class BmpTests extends TestCase {
	
	public void testScanlineSize() {
		byte[] array = new byte[BmpEncoder.HEADER_SIZE];
		
		assertTrue( BmpEncoder.writeHeader(array, 0, 16, 10, 24) == 16*3 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 17, 10, 24) == 16*3+4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 18, 10, 24) == 16*3+8 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 19, 10, 24) == 16*3+12 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 20, 10, 24) == 20*3 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 21, 10, 24) == 20*3+4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 22, 10, 24) == 20*3+8 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 23, 10, 24) == 20*3+12 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 24, 10, 24) == 24*3 );
		
		assertTrue( BmpEncoder.writeHeader(array, 0, 16, 10, 32) == 16*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 17, 10, 32) == 17*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 18, 10, 32) == 18*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 19, 10, 32) == 19*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 20, 10, 32) == 20*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 21, 10, 32) == 21*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 22, 10, 32) == 22*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 23, 10, 32) == 23*4 );
		assertTrue( BmpEncoder.writeHeader(array, 0, 24, 10, 32) == 24*4 );
	}
}
