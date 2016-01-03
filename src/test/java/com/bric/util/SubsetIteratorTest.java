/*
 * @(#)CombinationIteratorTest.java
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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Test;

public class SubsetIteratorTest extends TestCase {

	@Test
	public void testIterator() {
		Set<String> sampleSet = new TreeSet<String>();
		sampleSet.add("A");
		sampleSet.add("B");
		sampleSet.add("C");
		sampleSet.add("D");
		
		String[][] expectedResults = new String[][] {
				{},
				{"A"},
				{"B"},
				{"C"},
				{"D"},
				{"A", "B"},
				{"A", "C"},
				{"A", "D"},
				{"B", "C"},
				{"B", "D"},
				{"C", "D"},
				{"A", "B", "C"},
				{"A", "B", "D"},
				{"A", "C", "D"},
				{"B", "C", "D"},
				{"A", "B", "C", "D"},
		};
		
		SubsetIterator<String> iter = new SubsetIterator<String>(sampleSet, true);
		int i = 0;
		while(iter.hasNext()) {
			Set<String> results = iter.next();
			String[] array = results.toArray(new String[results.size()]);
			assertTrue(Arrays.equals(expectedResults[i], array) );
			i++;
		}
		assertEquals(expectedResults.length, i);
	}
}
