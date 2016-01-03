/*
 * @(#)TschirnhausTransformation.java
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Transform a polynomial into a depressed polynomial.
 * @see <a href="http://en.wikipedia.org/wiki/Tschirnhaus_transformation">http://en.wikipedia.org/wiki/Tschirnhaus_transformation</a> 
*/
public class TschirnhausTransformation {
	/** The original zeroed expression before this transform was applied. */
	public final Expression originalExpression;

	/** The new zeroed expression after this transform was applied. */
	public final Expression resultingExpression;
	
	/** The old variable this transform removed. */
	public final String replacedVariable;

	/** The new variable this substitution introduced. */
	public final String newVariableName;

	/** The Definition that explains what <code>newVariableName</code> equals. */
	public final Definition definition;
	
	/** Create a new TschirnhausTransformation.
	 * 
	 * @param zeroedExpression the left hand side of a polynomial equation, where the right hand side is zero.
	 * @param variableToRemove the variable to remove
	 * @param newVariable the variable to introduce
	 */
	public TschirnhausTransformation(Expression zeroedExpression,String variableToRemove,String newVariable) throws IllegalSubstitutionException {
		this.newVariableName = newVariable;
		this.originalExpression = zeroedExpression;
		this.replacedVariable = variableToRemove;
		
		Map<Fraction, List<Term>> map = zeroedExpression.factor(variableToRemove);
		
		Iterator<Fraction> powers = map.keySet().iterator();
		if(powers.hasNext()==false)
			throw new IllegalArgumentException("no powers");
		
		Fraction highestPower = powers.next();
		Expression highestPowerExp = new Expression(map.get(highestPower));
		
		if(highestPowerExp.getTermCount()>1) {
			//we're not equipped to handle substitutions with multiple terms
			throw new IllegalSubstitutionException("cannot divide by "+highestPowerExp);
		}
		zeroedExpression = zeroedExpression.divideBy(highestPowerExp.getTerm(0));
		
		//our leading coefficient is now "1".
		
		map = zeroedExpression.factor(variableToRemove);
		Fraction secondExponent = highestPower.subtract(1);
		Expression bExpression;
		if(map.get(secondExponent)==null) {
			bExpression = new Expression();
		} else {
			bExpression = new Expression(map.get(secondExponent));
		}
		
		Expression newVariableDefinition = new Expression(new Term(newVariable)).add( 
				bExpression.divideBy(highestPower).negate() );
		
		definition = new Definition( variableToRemove, newVariableDefinition);
		
		resultingExpression = zeroedExpression.substitute(definition);
	}
}
