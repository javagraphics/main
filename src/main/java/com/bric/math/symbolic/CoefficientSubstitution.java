/*
 * @(#)CoefficientSubstitution.java
 *
 * $Date: 2014-11-27 07:55:25 +0100 (Do, 27 Nov 2014) $
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
package com.bric.math.symbolic;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class CoefficientSubstitution {
	public final Expression originalExpression;
	public final Expression newExpression;
	public final SortedSet<Definition> definitions = new TreeSet<Definition>();
	
	/**
	 * 
	 * @param expr the expression to factor
	 * @param factoredVariable the variable name to factor by
	 * @param coefficientBase if this is "x", then our coefficients will be "x_{0}", "x_{1}", etc.
	 */
	public CoefficientSubstitution(Expression expr,String factoredVariable,String coefficientBase) {
		originalExpression = expr;
		SortedMap<Fraction, List<Term>> factors = expr.factor(factoredVariable);
		int ctr = 1;
		Expression e = new Expression();
		for(Fraction key : factors.keySet()) {
			List<Term> terms = factors.get(key);
			String coefficientName = coefficientBase+"_{"+(ctr++)+"}";
			e = e.add( new Term(factoredVariable, key).multiply(new Term(coefficientName)) );
			definitions.add(new Definition(coefficientName, new Expression(terms)));
		}
		newExpression = e;
	}
}
