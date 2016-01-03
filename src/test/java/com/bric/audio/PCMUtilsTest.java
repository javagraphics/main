/*
 * @(#)PCMUtilsTest.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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
package com.bric.audio;

import junit.framework.TestCase;

/** Tests for the the PCMUtils class.
 *
 */
public class PCMUtilsTest extends TestCase {
	
	/** This tests some uses of PCMUtils.decodeSample(..) against fixed values.
	 * 
	 */
	public void testDecode() {
		byte[] data = new byte[5];
		data[3] = (byte)(255);
		data[4] = (byte)(255);
		assertEquals( 65535, PCMUtils.decodeSample(data, 3, 2, false, true) );
		assertEquals( -1, PCMUtils.decodeSample(data, 3, 2, true, true) );

		data[3] = (byte)(0);
		data[4] = (byte)(0);
		assertEquals( 0, PCMUtils.decodeSample(data, 3, 2, false, true) );
		assertEquals( 0, PCMUtils.decodeSample(data, 3, 2, true, true) );

		data[3] = (byte)(0);
		data[4] = (byte)(255);
		assertEquals( 0x00ff, PCMUtils.decodeSample(data, 3, 2, false, true) );
		assertEquals( 0x00ff, PCMUtils.decodeSample(data, 3, 2, true, true) );
		assertEquals( 0xff00, PCMUtils.decodeSample(data, 3, 2, false, false) );
		assertEquals(  - (0x00ff) - 1, PCMUtils.decodeSample(data, 3, 2, true, false) );

		data[3] = (byte)(255);
		data[4] = (byte)(0);
		assertEquals( 0xff00, PCMUtils.decodeSample(data, 3, 2, false, true) );
		assertEquals(  - (0x00ff) - 1, PCMUtils.decodeSample(data, 3, 2, true, true) );
		assertEquals( 0x00ff, PCMUtils.decodeSample(data, 3, 2, false, false) );
		assertEquals( 0x00ff, PCMUtils.decodeSample(data, 3, 2, true, false) );
		

		data[3] = (byte)(0x6E);
		data[4] = (byte)(0xC2);
		assertEquals( 0x6EC2, PCMUtils.decodeSample(data, 3, 2, false, true) );
		assertEquals( 0x6EC2, PCMUtils.decodeSample(data, 3, 2, true, true) );
		assertEquals( 0xC26E, PCMUtils.decodeSample(data, 3, 2, false, false) );
		assertEquals(  - (0x3D91) - 1, PCMUtils.decodeSample(data, 3, 2, true, false) );
		
		//some basic 1-byte figures:
		data[2] = (byte)(0x0);
		assertEquals( 0, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( 0, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0x7e);
		assertEquals( 126, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( 126, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0x7f);
		assertEquals( 127, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( 127, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0x80);
		assertEquals( 128, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( -128, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0x81);
		assertEquals( 129, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( -127, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0x7e);
		assertEquals( 126, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( 126, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0xfe);
		assertEquals( 254, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( -2, PCMUtils.decodeSample(data, 2, 1, true, false));
		data[2] = (byte)(0xff);
		assertEquals( 255, PCMUtils.decodeSample(data, 2, 1, false, false));
		assertEquals( -1, PCMUtils.decodeSample(data, 2, 1, true, false));
	}
	
	/** Having tested the decode() method, this makes sure the
	 * encode() method always matches the decode() method.
	 */
	public void testEncode() {
		byte[] pair = new byte[2];
		byte[] copy = new byte[2];

		for(int a = 0; a<256; a++) {
			pair[1] = (byte)( (a >> 0) & 0xff );
			
			int value = PCMUtils.decodeSample(pair, 1, 1, true, false);
			PCMUtils.encodeSample(value, copy, 1, 1, true, false);
			assertArrayEquals(pair, copy);

			value = PCMUtils.decodeSample(pair, 1, 1, true, true);
			PCMUtils.encodeSample(value, copy, 1, 1, true, true);
			assertArrayEquals(pair, copy);

			value = PCMUtils.decodeSample(pair, 1, 1, false, false);
			PCMUtils.encodeSample(value, copy, 1, 1, false, false);
			assertArrayEquals(pair, copy);
			
			value = PCMUtils.decodeSample(pair, 1, 1, false, true);
			PCMUtils.encodeSample(value, copy, 1, 1, false, true);
			assertArrayEquals(pair, copy);
		}
		
		for(int a = 0; a<65536; a++) {
			pair[0] = (byte)( (a >> 8) & 0xff );
			pair[1] = (byte)( (a >> 0) & 0xff );
			
			int value = PCMUtils.decodeSample(pair, 0, 2, true, false);
			PCMUtils.encodeSample(value, copy, 0, 2, true, false);
			assertArrayEquals(pair, copy);
			
			value = PCMUtils.decodeSample(pair, 0, 2, true, true);
			PCMUtils.encodeSample(value, copy, 0, 2, true, true);
			assertArrayEquals(pair, copy);

			value = PCMUtils.decodeSample(pair, 0, 2, false, false);
			PCMUtils.encodeSample(value, copy, 0, 2, false, false);
			assertArrayEquals(pair, copy);

			value = PCMUtils.decodeSample(pair, 0, 2, false, true);
			PCMUtils.encodeSample(value, copy, 0, 2, false, true);
			assertArrayEquals(pair, copy);
		}
	}
	
	protected void assertArrayEquals(byte[] array1,byte[] array2) {
		assertEquals(array1.length, array2.length);
		StringBuffer sb1 = new StringBuffer();
		StringBuffer sb2 = new StringBuffer();
		for(int a = 0; a<array1.length; a++) {
			encode(sb1, array1[a]);
			encode(sb2, array2[a]);
		}
		assertEquals(sb1.toString(), sb2.toString());
	}
	
	private void encode(StringBuffer sb,byte b) {
		String s = Integer.toString( b & 0xff, 16 );
		while( s.length()<2 ) {
			s = "0"+s;
		}
		sb.append(s);
	}

}
