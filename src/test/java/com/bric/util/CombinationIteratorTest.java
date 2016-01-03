/*
 * @(#)CombinationIteratorTest.java
 *
 * $Date: 2015-09-13 20:46:53 +0200 (So, 13 Sep 2015) $
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
package com.bric.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import junit.framework.TestCase;

public class CombinationIteratorTest extends TestCase 
{
	@Test
	public void testCombinations() {
		List<String[]> input = new ArrayList<>();
		input.add(new String[] {"1", "2", "3"});
		input.add(new String[] {"apple", "orange"});
		input.add(new String[] {"xyz"});
		CombinationIterator<String> iter = new CombinationIterator<>(input);
		Set<List<String>> results = new HashSet<>();
		while(iter.hasNext()) {
			List<String> combo = iter.next();
			results.add(combo);
		}
		assertContains(results, "1", "apple", "xyz");
		assertContains(results, "1", "orange", "xyz");
		assertContains(results, "2", "apple", "xyz");
		assertContains(results, "2", "orange", "xyz");
		assertContains(results, "3", "apple", "xyz");
		assertContains(results, "3", "orange", "xyz");
		assertEquals(6, results.size());
	}
	
	protected void assertContains(Set<List<String>> results,String... strings) {
		List<String> list = new ArrayList<>();
		for(int a = 0; a<strings.length; a++) {
			list.add(strings[a]);
		}
		assertTrue( "results did not contain "+list, results.contains(list) );
	}
}
