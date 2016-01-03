/*
 * @(#)FractionalQuarticElimination.java
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


/** This transform an Expression with quartic roots of a variable into an
 * expression with no quartic roots.
 */
public class FractionalQuarticElimination extends FractionalExponentElimination {

	public static final Expression formula = new Expression(
			new Term(4, K, C[2], C[2], C[0], C[0]),
			new Term(-4, K, C[1], C[1], C[2], C[0]),
			new Term(-4, K, K, C[2], C[3], C[3], C[0]),
			new Term(K, C[1], C[1], C[1], C[1]),
			new Term(2, K, K, C[1], C[1], C[3], C[3]),
			new Term(K, K, K, C[3], C[3], C[3], C[3]),
			
			new Term(-4, K, K, C[1], C[1], C[3], C[3]),
			new Term(4, K, K, C[1], C[2], C[2], C[3]),
			new Term(4, K, C[1], C[3], C[0], C[0]),
			new Term(-1, K, K, C[2], C[2], C[2], C[2]),
			new Term(-2, K, C[2], C[2], C[0], C[0]),
			new Term(-1, C[0], C[0], C[0], C[0])
	);
	
	public FractionalQuarticElimination(Expression zeroedExpression,String variableName) {
		super(4, zeroedExpression, variableName);
	}
	
	@Override
	protected Expression getFormula() {
		return formula;
	}
}
