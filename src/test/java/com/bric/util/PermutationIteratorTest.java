/*
 * @(#)PermutationIteratorTest.java
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
import java.util.List;

import junit.framework.TestCase;

public class PermutationIteratorTest extends TestCase
{
	
	/** Test a few simple permutations up to 4-elements long. */
	public void testPermutations() {
		testPermutations("A");
		testPermutations("AB", "BA");
		testPermutations("ABC", "ACB", "BAC", "BCA", "CAB", "CBA");
		testPermutations("ABCD", "ABDC", "ACBD", "ACDB", "ADBC", "ADCB",
						 "BACD", "BADC", "BCAD", "BCDA", "BDAC", "BDCA",
						 "CABD", "CADB", "CBAD", "CBDA", "CDAB", "CDBA", 
						 "DABC", "DACB", "DBAC", "DBCA", "DCAB", "DCBA");
	}
	
	protected void testPermutations(String... expectedPermutations) {
		List<String> chars = new ArrayList<>();
		for(int a = 0; a<expectedPermutations[0].length(); a++) {
			char ch = expectedPermutations[0].charAt(a);
			chars.add( Character.toString(ch) );
		}
		
		PermutationIterator<String> iter = new PermutationIterator<>(chars);
		int ctr = 0;
		while(ctr<expectedPermutations.length) {
			List<String> iterValue = iter.next();
			String sum = "";
			for(int a = 0; a<iterValue.size(); a++) {
				sum += iterValue.get(a);
			}
			assertEquals(expectedPermutations[ctr], sum);
			ctr++;
		}
		assertFalse(iter.hasNext());
	}
	
	/** Test the special case of an empty list.
	 */
	public void testZeroCase() {
		PermutationIterator<String> iter = new PermutationIterator<>(new ArrayList<String>());
		assertTrue(iter.hasNext());
		List<String> iterResult = iter.next();
		assertEquals(0, iterResult.size());
		assertFalse(iter.hasNext());
	}
	
	public void testFactorial() {
		long factorial = 1;
		for(int a = 1; a<1000; a++) {
			try{
				factorial *= a;
				assertEquals(factorial, PermutationIterator.getFactorial(a) );
			} catch(ArithmeticException e) {
				System.err.println("factorials cannot be calculated starting at "+a);
				return;
			}
		}
	}
}
