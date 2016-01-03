/*
 * @(#)ReduceZeroedExpression.java
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

/** This transform reduces a zeroed expression by a common divisor.
 * For example, if the original expression is (5*a*b+15*a) then the reduced
 * expression will be (b+5)
 */
public class ReduceZeroedExpression {
	/** The original zeroed expression before this transform was applied. */
	public final Expression originalZeroedExpression;

	/** The new zeroed expression after this transform was applied. */
	public final Expression reducedZeroedExpression;
	
	/** Whether this transform was able to reduce anything. This is
	 * equivalent to (!originalZeroedExpression.equals(reducedZeroedExpression))
	 */
	public final boolean changed;
	
	public ReduceZeroedExpression(Expression zeroedExpression) {
		originalZeroedExpression = zeroedExpression;
		
		Term commonDivisorTerm = null;
		Fraction commonDivisorFraction = null;
		for(int a = 0; a<originalZeroedExpression.getTermCount(); a++) {
			Term t = originalZeroedExpression.getTerm(a);
			if(a==0) {
				commonDivisorTerm = t;
				commonDivisorFraction = t.coefficient;
			} else {
				commonDivisorTerm = commonDivisorTerm.getCommonElements(t);
				commonDivisorFraction = commonDivisorFraction.getCommonElements(t.coefficient);
			}
		}
		
		reducedZeroedExpression = originalZeroedExpression.divideBy(commonDivisorTerm.multiply(new Term(commonDivisorFraction)));
		changed = !originalZeroedExpression.equals(reducedZeroedExpression);
	}
}
