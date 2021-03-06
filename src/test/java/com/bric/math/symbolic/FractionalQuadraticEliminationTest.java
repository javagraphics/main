/*
 * @(#)FractionalQuadraticEliminationTest.java
 *
 * $Date: 2014-11-27 07:56:11 +0100 (Do, 27 Nov 2014) $
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

public class FractionalQuadraticEliminationTest extends FractionalExponentEliminationTest {
    
    public FractionalQuadraticEliminationTest(Long seed) {
    	super(seed);
    }
    
    @Override
    protected FractionalExponentElimination getElimination(Expression zeroedExpression, String variableName) {
		return new FractionalQuadraticElimination(zeroedExpression, variableName);
	}
    
    @Override
    protected int getDegree() {
    	return 2;
    }
}
