/*
 * @(#)FractionTests.java
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

import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Test;

public class FractionTests extends TestCase {
	
	@Test
	public void testMinMax() {
		{
			Fraction f1 = Fraction.get(1,2);
			Fraction f2 = Fraction.get(3,4);
			assertEquals(f1, Fraction.min(f1, f2));
			assertEquals(f2, Fraction.max(f1, f2));
		}
		
		{
			Fraction f1 = Fraction.get(-1,2);
			Fraction f2 = Fraction.get(-3,4);
			assertEquals(f1, Fraction.max(f1, f2));
			assertEquals(f2, Fraction.min(f1, f2));
		}

		{
			Fraction f1 = Fraction.get(-1,2);
			Fraction f2 = Fraction.get(1,4);
			assertEquals(f1, Fraction.min(f1, f2));
			assertEquals(f2, Fraction.max(f1, f2));
		}
	}
	
	@Test
	public void testIsolateDenominator() {
		{
			Fraction f1 = Fraction.get( 2*13*7, 3*3*5 );
			Fraction f2 = Fraction.get( 1, 3*3*5 );
			assertEquals(f2, f1.isolateDenominator() );
		}
		
		{
			Fraction f1 = Fraction.get( 2*5*7, 3*3*5 );
			Fraction f2 = Fraction.get( -11*13*2 , 23*3*5 );
			Fraction f3 = Fraction.get( 1, 3 );
			assertEquals(f3, f1.isolateCommonDenominator(f2) );
		}
	}
	
	@Test
	public void testLongValue() {
		Fraction f = Fraction.get(64);
		assertEquals(64L, f.longValue().longValue() );
		
		f = Fraction.get(2);
		Fraction bigFraction = Fraction.get(2);
		for(int a = 0; a<62; a++) {
			bigFraction = bigFraction.multiply(f);
		}
		assertEquals(null, bigFraction.longValue() );
		assertTrue( bigFraction.bigIntegerValue()!=null );

		f = Fraction.get(1, 2);
		assertEquals( null, f.longValue() );
	}
	
	@Test
	public void testReduction() {
		Fraction f1 = Fraction.get(32, 64);
		Fraction f2 = Fraction.get(1, 2);
		Fraction f3 = Fraction.get(5, 10);
		assertEquals(f1, f2);
		assertEquals(f2, f3);
		assertEquals(f1, f3);
		Fraction f4 = Fraction.get(-32, 64);
		Fraction f5 = Fraction.get(2, 2);
		Fraction f6 = Fraction.get(4, 10);
		assertFalse( f1.equals(f4) );
		assertFalse( f1.equals(f5) );
		assertFalse( f1.equals(f6) );
	}
	
	@Test
	public void testMultiply() {
		{
			Fraction f1 = Fraction.get(2,3);
			Fraction f2 = Fraction.get(-5,7);
			Fraction f3 = f1.multiply(f2);
			assertEquals( Fraction.get(-10, 21), f3 );
		}

		{
			Fraction f1 = Fraction.get(1,4);
			Fraction f2 = Fraction.get(2);
			Fraction f3 = f1.multiply(f2);
			assertEquals( Fraction.get(1, 2), f3 );
		}
	}
	
	@Test
	public void testAdd() {
		{
			Fraction f1 = Fraction.get(1,3);
			Fraction f2 = Fraction.get(1,2);
			Fraction f3 = f1.add(f2);
			assertEquals( Fraction.get(5, 6), f3 );
		}
		
		{
			Fraction f1 = Fraction.get(2*2*2*3*3*3);
			Fraction f2 = Fraction.get(2*2*5);
			Fraction f3 = f1.add(f2);
			assertEquals( Fraction.get(2*2*(2*3*3*3+5)), f3 );
		}
	}
	
	@Test
	public void testEquals() {
		{
			Fraction f1 = Fraction.get(4);
			Fraction f2 = Fraction.get(4);
			assertTrue( f1.equals(f2) );
			assertTrue( f1.equals(4) );
		}
	}
	
	@Test
	public void testSubtract() {
		{
			Fraction f1 = Fraction.get(1,3);
			Fraction f2 = Fraction.get(1,2);
			Fraction f3 = f1.subtract(f2);
			assertEquals( Fraction.get(-1, 6), f3 );
		}
	}
	
	@Test
	public void testRaiseToPower() throws IrrationalException, ImaginaryException {
		{
			Fraction f1 = Fraction.get(2,3);
			f1 = f1.raiseToPower(2);
			assertEquals( Fraction.get(4, 9), f1);
		}

		{
			Fraction f1 = Fraction.get(2,3);
			f1 = f1.raiseToPower(3);
			assertEquals( Fraction.get(8, 27), f1);
		}

		{
			Fraction f1 = Fraction.get(-2,3);
			f1 = f1.raiseToPower(2);
			assertEquals( Fraction.get(4, 9), f1);
		}

		{
			Fraction f1 = Fraction.get(-2,3);
			f1 = f1.raiseToPower(3);
			assertEquals( Fraction.get(-8, 27), f1);
		}
		
		/////////

		{
			Fraction f1 = Fraction.get(8);
			f1 = f1.raiseToPower(Fraction.get(2,3));
			assertEquals( Fraction.get(4), f1);
		}

		{
			Fraction f1 = Fraction.get(-8);
			f1 = f1.raiseToPower(Fraction.get(2,3));
			assertEquals( Fraction.get(4), f1);
		}

		{
			Fraction f1 = Fraction.get(-27);
			f1 = f1.raiseToPower(Fraction.get(2,3));
			assertEquals( Fraction.get(9), f1);
		}

		{
			Fraction f1 = Fraction.get(-27);
			f1 = f1.raiseToPower(Fraction.get(1,3));
			assertEquals( Fraction.get(-3), f1);
		}
	}
	
	@Test
	public void testRoot() throws IrrationalException, ImaginaryException {
		{
			Fraction f1 = Fraction.get(27);
			f1 = f1.getRoot(3);
			assertEquals( Fraction.get(3), f1);
		}
		
		
		{
			try {
				Fraction f1 = Fraction.get(25);
				f1 = f1.getRoot(3);
				fail();
			} catch(IrrationalException e) {
				//pass
			}
		}
		
		{
			Fraction f1 = Fraction.get(25);
			f1 = f1.getRoot(2);
			assertEquals( Fraction.get(5), f1);
		}
	}
	
	@Test
	public void testCompareTo() {
		Fraction f1 = Fraction.get(-5);
		Fraction f2 = Fraction.get(-5, 4);
		Fraction f3 = Fraction.get(-5, 5);
		Fraction f4 = Fraction.get(-3, 4);
		Fraction f5 = Fraction.get(-1, 4);
		Fraction f6 = Fraction.get(0);
		Fraction f7 = Fraction.get(1, 4);
		Fraction f8 = Fraction.get(3, 4);
		Fraction f9 = Fraction.get(5, 5);
		Fraction f10 = Fraction.get(5, 4);
		Fraction f11 = Fraction.get(5);
		
		Fraction[] f = new Fraction[] {
				f11,
				f3,
				f10,
				f6,
				f7,
				f4,
				f8,
				f1,
				f9,
				f2,
				f5
		};
		Arrays.sort(f);
		
		assertTrue(f[0]+" = "+f1, f[0]==f1);
		assertTrue(f[1]+" = "+f2, f[1]==f2);
		assertTrue(f[2]+" = "+f3, f[2]==f3);
		assertTrue(f[3]+" = "+f4, f[3]==f4);
		assertTrue(f[4]+" = "+f5, f[4]==f5);
		assertTrue(f[5]+" = "+f6, f[5]==f6);
		assertTrue(f[6]+" = "+f7, f[6]==f7);
		assertTrue(f[7]+" = "+f8, f[7]==f8);
		assertTrue(f[8]+" = "+f9, f[8]==f9);
		assertTrue(f[9]+" = "+f10, f[9]==f10);
		assertTrue(f[10]+" = "+f11, f[10]==f11);
	}
}
