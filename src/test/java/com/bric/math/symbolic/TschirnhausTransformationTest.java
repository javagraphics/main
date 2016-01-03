/*
 * @(#)TschirnhausTransformationTest.java
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

public class TschirnhausTransformationTest extends TestCase {
	public void testTransform() throws Exception {
		simpleTest : {

			Expression e = new Expression(
					new Term(1, 1, "z", "z", "z"),
					new Term(1, 1, "b", "z", "z"),
					new Term(1, 1, "c", "z"),
					new Term(1, 1, "d")
			);
			
			TschirnhausTransformation t = new TschirnhausTransformation(e, "z", "t");
			assertTrue(t.resultingExpression.toString(), t.resultingExpression.factor("t").get(Fraction.get(2))==null);
		}
	
		complexTest : {
			Expression e = new Expression(
					new Term(5, 3, "a", "z", "z", "z"),
					new Term(1, 7, "b", "z", "z"),
					new Term(2, 11, "c", "z"),
					new Term(13, 6, "d")
			);
			
			TschirnhausTransformation t = new TschirnhausTransformation(e, "z", "t");
			System.out.println(t.resultingExpression.toString("t", OutputFormat.PLAIN));
			assertTrue(t.resultingExpression.toString(), t.resultingExpression.factor("t").get(Fraction.get(2))==null);
		}
	}
}
