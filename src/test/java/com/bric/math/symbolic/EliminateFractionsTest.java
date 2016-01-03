/*
 * @(#)EliminateFractionsTest.java
 *
 * $Date: 2014-11-27 07:56:11 +0100 (Do, 27 Nov 2014) $
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
package com.bric.math.symbolic;

import junit.framework.TestCase;

import org.junit.Test;

import com.bric.math.symbolic.Expression.DivisionResult;

public class EliminateFractionsTest extends TestCase {
	
	@Test
	public void testConstantDenominators() {
		{
			Expression e = new Expression(new Term(1,3,"a"), new Term(1,2,"b"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(2, "a"), new Term(3, "b")), r.quotient);
			assertEquals(new Term(6), r.divisor);
		}
		
		{
			Expression e = new Expression(new Term(1,3,"a"), new Term(1,6,"b"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(2, "a"), new Term(1, "b")), r.quotient);
			assertEquals(new Term(6), r.divisor);
		}
		
		{
			Expression e = new Expression(new Term(1,5,"a"), new Term(1,125,"b"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(25, "a"), new Term(1, "b")), r.quotient);
			assertEquals(new Term(125), r.divisor);
		}
	}
	
	@Test
	public void testNonTrivialNumerator() {
		{
			Expression e = new Expression(new Term(69,5,"a"), new Term(24,125,"b"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(69*25, "a"), new Term(24, "b")), r.quotient);
			assertEquals(new Term(125), r.divisor);
		}
	}
	
	@Test
	public void testVariableDenominator() {
		{
			Expression e = new Expression(new Term(1,3,"a").divideBy("b"), new Term(1,2,"b"));

			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(2, "a"), new Term(3, "b", "b")), r.quotient);
			assertEquals(new Term(6, "b"), r.divisor);
		}
		
		{
			Expression e = new Expression(new Term(1,3,"a"), new Term(1,6,"b").divideBy("a","c"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(2, "a", "a", "c"), new Term(1, "b")), r.quotient);
			assertEquals(new Term(6, "a", "c"), r.divisor);
		}
		
		{
			Expression e = new Expression(new Term(1,5,"a").divideBy("b", "b"), new Term(1,125,"b"));
			DivisionResult r = e.eliminateFractions();
			assertEquals(new Expression(new Term(25, "a"), new Term(1, "b", "b", "b")), r.quotient);
			assertEquals(new Term(125, "b", "b"), r.divisor);
		}
	}
}
