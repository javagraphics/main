/*
 * @(#)LookAheadReaderTest.java
 *
 * $Date: 2015-12-26 08:54:45 +0100 (Sa, 26 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
package com.bric.io;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

public class LookAheadReaderTest extends TestCase {

	public void testReadChars() throws IOException {
		{
			String src = "abcDEF";
			char[] cbuf = new char[3];
			try(LookAheadReader reader = new LookAheadReader(new StringReader(src))) {
				assertEquals(2, reader.skip(2));
				assertEquals(3, reader.read(cbuf));
				assertEquals('c', cbuf[0]);
				assertEquals('D', cbuf[1]);
				assertEquals('E', cbuf[2]);
				assertEquals(1, reader.read(cbuf));
				assertEquals('F', cbuf[0]);
				assertEquals(0, reader.read(cbuf));
			}
		}
		
		{
			String src = "mnopqrstuvwxyz  abcdefghijklMNOPQRSTUVWXYZ 123456789";
			char[] cbuf = new char[28];
			try(LookAheadReader reader = new LookAheadReader(new StringReader(src), 28)) {
				assertEquals(16, reader.skip(16));
				assertEquals(28, reader.read(cbuf));
				assertEquals('a', cbuf[0]);
				assertEquals('b', cbuf[1]);
				assertEquals('c', cbuf[2]);
				assertEquals('d', cbuf[3]);
				assertEquals('e', cbuf[4]);
				assertEquals('f', cbuf[5]);
				assertEquals('g', cbuf[6]);
				assertEquals('h', cbuf[7]);
				assertEquals('i', cbuf[8]);
				assertEquals('j', cbuf[9]);
				assertEquals('k', cbuf[10]);
				assertEquals('l', cbuf[11]);
				assertEquals('M', cbuf[12]);
				assertEquals('N', cbuf[13]);
				assertEquals('O', cbuf[14]);
				assertEquals('P', cbuf[15]);
				assertEquals('Q', cbuf[16]);
				assertEquals('R', cbuf[17]);
				assertEquals('S', cbuf[18]);
				assertEquals('T', cbuf[19]);
				assertEquals('U', cbuf[20]);
				assertEquals('V', cbuf[21]);
				assertEquals('W', cbuf[22]);
				assertEquals('X', cbuf[23]);
				assertEquals('Y', cbuf[24]);
				assertEquals('Z', cbuf[25]);
				assertEquals(' ', cbuf[26]);
				assertEquals('1', cbuf[27]);
			}
		}
	}

}
