/*
 * @(#)FractionalExponentElimination.java
 *
 * $Date: 2014-11-27 07:55:25 +0100 (Do, 27 Nov 2014) $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This is an abstract framework to apply a transform to remove the fractional exponents from a zeroed expression.
 */
public abstract class FractionalExponentElimination implements FEEConstants {
	
	private static void validateVariableNames(Expression e,String... illegalVariableNames) {
		Set<String> vars = e.getVariables();
		for(String s : illegalVariableNames) {
			if(vars.contains(s))
				throw new IllegalArgumentException("the variable name \""+s+"\" is reserved");
		}
	}

	/** The original zeroed expression before this transform was applied. */
	public final Expression originalExpression;

	/** The new zeroed expression after this transform was applied. */
	public final Expression resultingExpression;
	
	/** The variable we need to transform. */
	public final String variableName;
	
	/** Create a FractionalExponentElimination.
	 * 
	 * @param degree the degree this transform applies to.
	 * @param zeroedExpression an expression equal to zero to transform.
	 * @param variableName the variable to transform.
	 */
	public FractionalExponentElimination(int degree, Expression zeroedExpression,String variableName) {
		if(degree<=1) throw new IllegalArgumentException("degree = "+degree);
		
		validateVariableNames(zeroedExpression, K);
		validateVariableNames(zeroedExpression, C);
		
		this.variableName = variableName;
		this.originalExpression = zeroedExpression;

		zeroedExpression = zeroedExpression.eliminateFractions().quotient;
		
		Map<Fraction, List<Term>> f = zeroedExpression.factor(variableName);
		
		/** Each array element is for k^(index/degree) terms */
		Expression[] e = new Expression[degree];
		for(int a = 0; a<e.length; a++) {
			e[a] = new Expression();
		}
		
		Iterator<Fraction> powers = f.keySet().iterator();
		while(powers.hasNext()) {
			Fraction power = powers.next();
			Expression phrase = new Expression(f.get(power));
			Fraction z = power.multiply(Fraction.get(degree));
			if(!z.isInteger())
				throw new IllegalArgumentException("identified a power that is not a factor of degree ("+power+")");
			int i = z.intValue();
			int mod = i%degree;
			int div = i/degree;
			if(div>0) {
				phrase = phrase.multiply(new Term(variableName, Fraction.get(div)));
			}
			e[mod] = e[mod].add(phrase);
		}
		
		Expression k = new Expression(new Term(variableName));
		try {
			Expression result = getFormula();

			List<Definition> allSubstitutions = new ArrayList<Definition>();
			for(int a = 0; a<degree; a++) {
				allSubstitutions.add(new Definition(C[a], e[a]));
			}
			allSubstitutions.add(new Definition(K, k));
			resultingExpression = result.substitute(allSubstitutions.toArray(new Definition[allSubstitutions.size()]));
		} catch(IllegalSubstitutionException e2) {
			throw new IllegalArgumentException("could not transform this expression", e2);
		}
	}
	
	/** @return the formula this elimination/transformation applies, stated in terms of FEEConstants. */
	protected abstract Expression getFormula();
}
