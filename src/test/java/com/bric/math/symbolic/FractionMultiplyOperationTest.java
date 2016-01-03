/*
 * @(#)FractionMultiplyOperationTest.java
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


public class FractionMultiplyOperationTest extends AbstractFractionOperationTest {

	public FractionMultiplyOperationTest(long randomSeed) {
		super(randomSeed);
	}

	public void testOperation(Fraction f1,Fraction f2) {
		Fraction product = f1.multiply(f2);
		double v1 = f1.doubleValue();
		double v2 = f2.doubleValue();
		double realProduct = v1*v2;
		
		double tolerance = Math.abs(realProduct/10000);
		
		boolean closeEnough = Math.abs(realProduct-product.doubleValue())<tolerance;
		if(!closeEnough)
			fail(f1+"*"+f2+" was "+product+", expected "+realProduct);
	}
}
