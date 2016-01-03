/*
 * @(#)ReflectionTest.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
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
package com.bric.util;

import java.awt.Color;

import junit.framework.TestCase;

import com.bric.reflect.Reflection;

public class ReflectionTest extends TestCase {
	public void testParseColor1() {
		assertEquals( new Color(255,100,9), Reflection.parse("new java.awt.Color(255,100,9)") );
	}
	
	public void testParseColor2() {
		assertEquals( Color.lightGray, Reflection.parse("java.awt.Color.lightGray") );
	}
	
	public void testParseString1() {
		assertEquals( "abc", Reflection.parse("\"abc\"") );
	}
}
