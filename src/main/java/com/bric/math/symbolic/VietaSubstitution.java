/*
 * @(#)VietaSubstitution.java
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

import java.util.List;
import java.util.Map;

/** Transform a standard cubic equation to one that is more similar a quadratic.
 * @see <a href="http://mathworld.wolfram.com/VietasSubstitution.html">http://mathworld.wolfram.com/VietasSubstitution.html</a>
 */
public class VietaSubstitution {
	/** The original zeroed expression before this substitution was applied. */
	public final Expression originalExpression;
	
	/** The new zeroed expression after this substitution was applied. */
	public final Expression resultingExpression;
	
	/** The old variable this substitution removed. */
	public final String replacedVariable;

	/** The new variable this substitution introduced. */
	public final String newVariableName;

	/** The Definition that explains what <code>newVariableName</code> equals. */
	public final Definition definition;
	
	/** Create a new VietaSubstitution.
	 * 
	 * @param zeroedExpression the left hand side of a standard cubic equation, where the right hand side is assumed to be zero.
	 * @param variableToRemove the variable to remove from this equation.
	 * @param newVariable the new variable to introduce.
	 * @throws IllegalSubstitutionException
	 */
	public VietaSubstitution(Expression zeroedExpression,String variableToRemove,String newVariable) throws IllegalSubstitutionException {
		if(!isStandard(zeroedExpression, variableToRemove))
			throw new IllegalArgumentException("the expression is not a standard cubic equation using the form "+variableToRemove+"^2 + p*"+variableToRemove+" - q");
		
		replacedVariable = variableToRemove;
		newVariableName = newVariable;
		originalExpression = zeroedExpression;
		
		Map<Fraction, List<Term>> factors = zeroedExpression.factor(variableToRemove);
		Expression p = new Expression(factors.get(Fraction.get(1)));
		Expression newDefinition = new Expression(
				new Term(newVariableName) ).add(
						p.negate().divideBy( new Term(3, 1, newVariableName) )
						);
		definition = new Definition(replacedVariable, newDefinition);
		resultingExpression = originalExpression.substitute(definition);
	}

	private boolean isStandard(Expression zeroedExpression,
			String variableName) {
		Map<Fraction, List<Term>> factors = zeroedExpression.factor(variableName);
		if(factors.remove(Fraction.get(3))==null)
			return false;
		factors.remove(Fraction.get(1));
		factors.remove(Fraction.get(0));
		return factors.size()==0;
	}
}
