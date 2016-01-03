/*
 * @(#)FractionalQuadraticElimination.java
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

/** This transform an Expression with square roots of a variable into an
 * expression with no square roots.
 */
public class FractionalQuadraticElimination extends FractionalExponentElimination {
	
	public static final Expression formula = new Expression(
			new Term(C[1], C[1], K),
			new Term(-1, C[0], C[0])
	);
	
	public FractionalQuadraticElimination(Expression zeroedExpression,String variableName) {
		super(2, zeroedExpression, variableName);
	}
	
	@Override
	protected Expression getFormula() {
		return formula;
	}
}

