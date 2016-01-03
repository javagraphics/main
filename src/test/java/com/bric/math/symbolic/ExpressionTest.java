/*
 * @(#)ExpressionTest.java
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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.junit.Test;

public class ExpressionTest extends TestCase {

	@Test
	public void testEvaluation() throws MissingVariablesException {
		{
			Expression e = new Expression(new Term(1, 6, "a"));
			Map<String, Number> variables = new HashMap<String, Number>();
			variables.put("a", 3);
			variables.put("b", 7);
			Number n = e.evaluate(variables);
			assertEquals(.5, n);
		}
	}
}
