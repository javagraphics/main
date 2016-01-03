/*
 * @(#)AbstractFractionOperationTest.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractFractionOperationTest extends TestCase {

	@Parameters
    public static Collection<Long[]> params() {
    	List<Long[]> returnValue = new ArrayList<Long[]>();
    	for(long t = 0; t<=1000; t++) {
    		returnValue.add(new Long[] { t });
    	}
    	return returnValue;
    }
    
    public final long randomSeed;
    
    public AbstractFractionOperationTest(long randomSeed) {
    	this.randomSeed = randomSeed;
    }

    private Long createLong(Random r,boolean allowZero) {
    	while(true) {
    		int i = r.nextInt(16777216)-16777216/2;
    		if(allowZero || i!=0)
    			return new Long(i);
    	}
    }
    
	@Test
	public void testUsingRandoms() {
		Random random = new Random(randomSeed);
		Long n1 = createLong(random, true);
		Long n2 = createLong(random, true);
		Long d1 = createLong(random, false);
		Long d2 = createLong(random, false);
		
		Fraction f1 = Fraction.get(n1, d1);
		Fraction f2 = Fraction.get(n2, d2);
		testOperation(f1, f2);
	}
	
	public abstract void testOperation(Fraction f1,Fraction f2);
	
	static long[] smallPrimes = new long[] {2, 3, 5, 7, 11, 13};
	
	@Test
	public void testUsingSmallPrimes() {
		Random random = new Random(randomSeed);
		Fraction f1 = createFractionUsingSmallPrimes(random);
		Fraction f2 = createFractionUsingSmallPrimes(random);
		testOperation(f1, f2);
	}
	
	private Fraction createFractionUsingSmallPrimes(Random r) {
		long numerator = 1;
		long denominator = 1;
		for(long prime : smallPrimes) {
			int power = r.nextInt(5);
			while(power>0) {
				numerator = numerator * prime;
				power--;
			}
			power = r.nextInt(5);
			while(power>0) {
				denominator = denominator * prime;
				power--;
			}
		}
		if(r.nextBoolean()) {
			numerator = -numerator;
		}
		return Fraction.get(numerator, denominator);
	}
}
