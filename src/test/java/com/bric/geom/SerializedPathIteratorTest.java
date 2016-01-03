/*
 * @(#)SerializedPathIteratorTest.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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
package com.bric.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import junit.framework.TestCase;

import com.bric.reflect.Reflection;

public class SerializedPathIteratorTest extends TestCase {
	public void testSimple() {
		GeneralPath path = new GeneralPath();
		path.moveTo(5, 3);
		path.lineTo(2, 1);
		path.quadTo(8, 7, 2, 4);
		path.curveTo(7, 6, 9, 3, 8, 2);
		path.moveTo(3, 5);
		path.closePath();
		String s = ShapeStringUtils.toString(path);
		assertTrue(s.equals("m 5.0 3.0 l 2.0 1.0 q 8.0 7.0 2.0 4.0 c 7.0 6.0 9.0 3.0 8.0 2.0 m 3.0 5.0 z"));
		testShapeVsString( path, s, 1);
		//test for support for a lack of decimals:
		testShapeVsString( path, "m 5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z", 1);
		//test for support for ridiculous whitespace:
		testShapeVsString( path, " \t\tm  5\n   3.0\r   l  \t2.0  1  q  8  7.0    2 4.0     c  7.0   6   9.0 3    8.0  2 m   3.0   5 z     ", 1);

		testStringPasses("m 4.99999998E11 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E10");
		testStringPasses("m 4.99999998E11 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998");
	}
	
	public void testBigNumbers() {
		GeneralPath path = new GeneralPath();
		path.moveTo(500000000000f, 300000000000f);
		path.lineTo(200000000000f, 100000000000f);
		path.quadTo(800000000000f, 700000000000f, 200000000000f, 400000000000f);
		path.curveTo(700000000000f, 600000000000f, 900000000000f, 300000000000f, 800000000000f, 200000000000f);
		path.moveTo(300000000000f, 500000000000f);
		path.closePath();
		String s = ShapeStringUtils.toString(path);
		testShapeVsString( path, s, 100000000000f);
	}
	
	public void testBadPaths() {
		//these strings should fail:
		testStringFails("w 5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5.. 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5.2.4 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5 3.0 l 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 9 m 3.0 5 z");
		testStringFails("mm 5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5 3.0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6k 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5 3. 0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 5 3 .0 l 2.0 1 q 8 7.0 2 4.0 c 7.0 6 9.0 3 8.0 2 m 3.0 5 z");
		testStringFails("m 4.99999998E.11 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 4.99999998E1.1 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 4.99999998E11. 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 4.99999998EE 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 4.99999998E 2.99999986E11 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 4.99999998E11 2.99999986E 1 l 1.99999996E11 9.9999998E10 q 7.9999998E11 6.9999998E11 1.99999996E11 3.99999992E11 c 6.9999998E11 5.9999997E11 8.9999999E11 2.99999986E11 7.9999998E11 1.99999996E11 m 2.99999986E11 4.99999998E11 z");
		testStringFails("m 2.99999986E11 4.99999998E11 zm 0 1");
		testStringFails("m 2.99999986E11 4.99999998E11 zz");
	}
	
	private void testStringFails(String s) {
		try {
			SerializedPathIterator iter = new SerializedPathIterator(s, PathIterator.WIND_EVEN_ODD);
			while(iter.isDone()==false) {
				iter.currentSegment(new double[6]);
				iter.next();
			}
			fail("the path \""+s+"\" was supposed to fail");
		} catch(Exception e) {
			return;
		}
	}
	
	private void testStringPasses(String s) {
		SerializedPathIterator iter = new SerializedPathIterator(s, PathIterator.WIND_EVEN_ODD);
		while(iter.isDone()==false) {
			iter.currentSegment(new double[6]);
			iter.next();
		}
	}

	public void testNegativeNumbers() {
		GeneralPath path = new GeneralPath();
		path.moveTo(-5, 3);
		path.lineTo(2, -1);
		path.quadTo(8, -7, 2, -4);
		path.curveTo(7, 6, -9, 3, -8, -2);
		path.moveTo(-3, -5);
		path.closePath();
		String s = ShapeStringUtils.toString(path);
		assertTrue(s.equals("m -5.0 3.0 l 2.0 -1.0 q 8.0 -7.0 2.0 -4.0 c 7.0 6.0 -9.0 3.0 -8.0 -2.0 m -3.0 -5.0 z"));
		testShapeVsString( path, s, 1);
	}

	public void testTinyNumbers() {
		GeneralPath path = new GeneralPath();
		path.moveTo(.000000000005f, .000000000003f);
		path.lineTo(.000000000002f, .000000000001f);
		path.quadTo(.000000000008f, .000000000007f, .000000000002f, .000000000004f);
		path.curveTo(.000000000007f, .000000000006f, .000000000009f, .000000000003f, .000000000008f, .000000000002f);
		path.moveTo(.000000000003f, .000000000005f);
		path.closePath();
		String s = ShapeStringUtils.toString(path);
		assertTrue(s.equals("m 5.0E-12 3.0E-12 l 2.0E-12 1.0E-12 q 8.0E-12 7.0E-12 2.0E-12 4.0E-12 c 7.0E-12 6.0E-12 9.0E-12 3.0E-12 8.0E-12 2.0E-12 m 3.0E-12 5.0E-12 z"));
		testShapeVsString( path, s, .000000000001f);
	}
	
	private void testShapeVsString(GeneralPath shape,String string,float dx) {
		SerializedPathIterator iter = new SerializedPathIterator(string, shape.getWindingRule());
		String diff = equals(iter, shape.getPathIterator(null));
		if(diff!=null)
			fail( diff );
		iter = new SerializedPathIterator(string, shape.getWindingRule());
		AffineTransform transform = AffineTransform.getTranslateInstance(dx, 0);
		diff = equals(iter, shape.getPathIterator(transform));
		if(diff==null)
			fail( "two paths appeared equal when they shouldn't be" );
	}
	
	public static String equals(PathIterator iter1,PathIterator iter2) {
		if(iter1.getWindingRule()!=iter2.getWindingRule())
			return "winding rules";
		double[] data1 = new double[6];
		double[] data2 = new double[6];
		int ctr = 0;
		while(true) {
			int k1 = iter1.currentSegment(data1);
			int k2 = iter2.currentSegment(data2);
			ctr++;
			if(k1!=k2)
				return "segment "+ctr+": "+
				Reflection.nameStaticField(PathIterator.class, new Integer(k1))+
				" vs "+
				Reflection.nameStaticField(PathIterator.class, new Integer(k2));
			int num;
			if(k1==PathIterator.SEG_MOVETO || k1==PathIterator.SEG_LINETO) {
				num = 2;
			} else if(k1==PathIterator.SEG_QUADTO) {
				num = 4;
			} else if(k1==PathIterator.SEG_CUBICTO) {
				num = 6;
			} else if(k1==PathIterator.SEG_CLOSE) {
				num = 0;
			} else {
				throw new RuntimeException("Unexpecting segment type: "+k1);
			}
			if(equals(data1,data2,num)==false)
				return "segment "+ctr+": "+toString(data1, num)+" vs "+toString(data2, num);

			iter1.next();
			iter2.next();
			
			if(iter1.isDone() && iter2.isDone())
				return null;
			if(iter1.isDone() || iter2.isDone())
				return "segment "+ctr+": "+iter1.isDone()+" vs "+iter2.isDone();
		}
	}
	
	public static String toString(double[] array,int length) {
		StringBuffer sb = new StringBuffer("[ ");
		for(int a = 0; a<length; a++) {
			if(a!=0) {
				sb.append(", ");
			}
			sb.append( Double.toString(array[a]) );
		}
		sb.append(" ]");
		return sb.toString();
	}
	
	public static boolean equals(double[] array1,double[] array2,int length) {
		double ln10 = Math.log(10);
		for(int a = 0; a<length; a++) {
			double exp1 = Math.floor( (Math.log(array1[a])/ln10) );
			double exp2 = Math.floor( (Math.log(array2[a])/ln10) );
			double expMin = Math.min(exp1, exp2);
			double tolerance = Math.pow(10, expMin)/1000;
			
			if(Math.abs(array1[a]-array2[a])>tolerance)
				return false;
		}
		return true;
	}
}
