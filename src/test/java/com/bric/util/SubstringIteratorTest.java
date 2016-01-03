/*
 * @(#)SubstringIteratorTest.java
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
package com.bric.util;

import junit.framework.TestCase;

public class SubstringIteratorTest extends TestCase {
	public void testIterator() {
		SubstringIterator iter = new SubstringIterator("abcde");
		String[] expectedResults = new String[] {
				"abcde", "abcd", "bcde", "abc", "bcd", "cde", "ab", "bc", "cd", "de", "a", "b", "c", "d", "e"
		};
		int ctr = 0;
		while(iter.hasNext()) {
			String value = iter.next();
			assertTrue("\""+value+"\" should equals \""+expectedResults[ctr]+"\"", 
					value.equals(expectedResults[ctr]));
			ctr++;
		}
		assertTrue(ctr==expectedResults.length);
	}
	
	public void testUniqueness() {
		SubstringIterator iter = new SubstringIterator("ababa", true);
		String[] expectedResults = new String[] {
				"ababa", "abab", "baba", "aba", "bab", "ab", "ba", "a", "b"
		};
		int ctr = 0;
		while(iter.hasNext()) {
			String value = iter.next();
			assertTrue("\""+value+"\" should equals \""+expectedResults[ctr]+"\"", 
					value.equals(expectedResults[ctr]));
			ctr++;
		}
		assertTrue(ctr==expectedResults.length);

		iter = new SubstringIterator("ababa", false);
		expectedResults = new String[] {
				"ababa", "abab", "baba", "aba", "bab", "aba", "ab", "ba", "ab", "ba", "a", "b", "a", "b", "a"
		};
		ctr = 0;
		while(iter.hasNext()) {
			String value = iter.next();
			assertTrue("\""+value+"\" should equals \""+expectedResults[ctr]+"\"", 
					value.equals(expectedResults[ctr]));
			ctr++;
		}
		assertTrue(ctr==expectedResults.length);
	}
}
