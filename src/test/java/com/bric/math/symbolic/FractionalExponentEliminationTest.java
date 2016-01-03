/*
 * @(#)FractionalExponentEliminationTest.java
 *
 * $Date: 2014-11-27 07:56:11 +0100 (Do, 27 Nov 2014) $
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class FractionalExponentEliminationTest extends TestCase {

    private static List<Long[]> createArray(long min,long max) {
    	List<Long[]> returnValue = new ArrayList<Long[]>();
    	for(long t = min; t<=max; t++) {
    		returnValue.add(new Long[] { t });
    	}
    	return returnValue;
    }
    
	@Parameters
    public static Collection<Long[]> params() {
        return createArray(1,50);
    }
    
    protected long seed;
	private Expression e = new Expression();
	Map<String, Number> variableMap = new HashMap<String, Number>();
    public FractionalExponentEliminationTest(Long seed) {
    	this.seed = seed;
		int d = getDegree();
		for(int a = 0; a<d*3; a++) {
			Expression z = new Expression( 
					new Term("c"+(a+1)).multiply(new Term("k", Fraction.get(a+1, d))) );
			e = e.add(z);
			
			//add a second term to make things more complicated
			z = new Expression( 
					new Term(2, "c"+(a+1)+"b").multiply(new Term("k", Fraction.get(a+1, d))) );
			e = e.add(z);
			
			//add a common term everyone shares:
			z = new Expression( 
					new Term(-5, "cz").multiply(new Term("k", Fraction.get(a+1, d))) );
			e = e.add(z);
		}
    }
    
    protected abstract int getDegree();
    
    @Test
	public void testElimination() throws MissingVariablesException {
		Random r = new Random(seed);
		int d = getDegree();
		variableMap.clear();
		
		int j = 2;
		Set<String> variables = e.getVariables();
		for(String variable : variables) {
			if(variable.startsWith("c")) {
				variableMap.put( variable, j-j*r.nextDouble());
			}
		}
		
		if(d%2==0) {
			//don't take an even root of a negative number
			variableMap.put("k", j*r.nextDouble());
		} else {
			variableMap.put("k", j-j*r.nextDouble());
		}
		
		//calibrate the whole equation to zero:
		double balancingTerm = -e.evaluate(variableMap);
		assertTrue(!variables.contains("c0"));
		variableMap.put("c0", balancingTerm);
		
		Expression complete = e.add(new Term("c0"));
		//this isn't what we're testing, but if this fails
		//then our whole setup is wrong:
		double v = complete.evaluate(variableMap);
		assertTrue( Math.abs(v)<.0001 );
		checkFractionalPowers(complete, "k", true);
		
		//here's the important part:
		FractionalExponentElimination elim = getElimination(complete, "k");
		Expression transformed = elim.resultingExpression;
		
		double value = transformed.evaluate(variableMap);
		assertTrue( value+" must be approximately zero (seed="+seed+")", Math.abs(value)<.001 );
		checkFractionalPowers(transformed, "k", false);
	}
    
    protected abstract FractionalExponentElimination getElimination(Expression zeroedExpression, String variableName);
	
	private static void checkFractionalPowers(Expression e,String variableName,boolean fractionalPowersExpected) {
		Map<Fraction, List<Term>> f = e.factor(variableName);
		Iterator<Fraction> powers = f.keySet().iterator();
		boolean foundFractionalPowers = false;
		while(powers.hasNext()) {
			Fraction power = powers.next();
			if(!power.isInteger()) {
				foundFractionalPowers = true;
			}
		}
		assertEquals(f.keySet().toString(), fractionalPowersExpected, foundFractionalPowers);
	}
}
