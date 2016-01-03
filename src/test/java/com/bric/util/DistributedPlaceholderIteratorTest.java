/*
 * @(#)DistributedPlaceholderIteratorTest.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
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
package com.bric.util;

import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Test;

public class DistributedPlaceholderIteratorTest extends TestCase {

	@Test
	public void testIterator() {
		DistributedPlaceholderIterator iter = new DistributedPlaceholderIterator(3,4,true);
		Set<String> values = new TreeSet<String>();
		long ctr = 0;
		while(!iter.isDone()) {
			String s = iter.toString();
			//System.out.println(s);
			if(!values.add(s))
				fail("already contains \""+s+"\"");
			ctr++;
			iter.next();
		}
		//There are 9 values [-4, -3, -2, -1, 0, 1, 2, 3, 4], repeated 3 times:
		assertEquals( 9*9*9, ctr );
	}


	@Test
	public void testPositiveIterator() {
		DistributedPlaceholderIterator iter = new DistributedPlaceholderIterator(3,4,false);
		Set<String> values = new TreeSet<String>();
		long ctr = 0;
		while(!iter.isDone()) {
			String s = iter.toString();
			//System.out.println(s);
			if(!values.add(s))
				fail("already contains \""+s+"\"");
			ctr++;
			iter.next();
		}
		//There are 5 values [0, 1, 2, 3, 4], repeated 3 times:
		assertEquals( 5*5*5, ctr );
	}
}
