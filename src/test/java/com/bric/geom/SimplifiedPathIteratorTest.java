/*
 * @(#)SimplifiedPathIteratorTest.java
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

import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;

import junit.framework.TestCase;

public class SimplifiedPathIteratorTest extends TestCase {
	public void testCollinearCubic() {
		testCollinearCubic( new CubicCurve2D.Float(50,50,100,100,200,200,300,300), true );
		//TODO: these tests currently fail
		//testCollinearCubic( new CubicCurve2D.Float(100,100,50,50,200,200,300,300), false );
		//testCollinearCubic( new CubicCurve2D.Float(100,100,50,50,300,300,200,200), false );
		//testCollinearCubic( new CubicCurve2D.Float(100,100,50,50,300,300,100,100), false );
	}
	
	private void testCollinearCubic(CubicCurve2D curve,boolean shouldSimplify) {
		PathIterator i = curve.getPathIterator(null);
		double[] coords = new double[6];
		double lastX = 0;
		double lastY = 0;
		while(i.isDone()==false) {
			int k = i.currentSegment(coords);
			k = SimplifiedPathIterator.simplify(k, lastX, lastY, coords);
			if(k==PathIterator.SEG_MOVETO) {
				lastX = coords[0];
				lastY = coords[1];
			} else if(k==PathIterator.SEG_LINETO) {
				if(!shouldSimplify)
					fail("this shape cannot be simplified to a SEG_LINETO, but it is");
				
				lastX = coords[0];
				lastY = coords[1];
			} else if(k==PathIterator.SEG_QUADTO) {
				if(shouldSimplify)
					fail("this shape can be simplified to a SEG_LINETO, but it isn't");
				
				lastX = coords[2];
				lastY = coords[3];
			} else if(k==PathIterator.SEG_CUBICTO) {
				if(shouldSimplify)
					fail("this shape can be simplified to a SEG_LINETO, but it isn't");
			
				lastX = coords[4];
				lastX = coords[5];
			}
			i.next();
		}
	}
	
	public void testNoncollinearCubic() {
		//TODO: calculate the magic control points involved to make a cubic curve
		//that simplifies to a quadratic.
		/*Shape shape = new CubicCurve2D.Float(50,50,100,50,100, 50, 100, 100);
		shape = AffineTransform.getRotateInstance( -Math.PI/4 ).createTransformedShape(shape);
		PathIterator i = shape.getPathIterator(null);
		double[] coords = new double[6];
		double lastX = 0;
		double lastY = 0;
		while(i.isDone()==false) {
			int k = i.currentSegment(coords);
			k = SimplifiedPathIterator.simplify(k, lastX, lastY, coords);
			if(k==PathIterator.SEG_MOVETO) {
				lastX = coords[0];
				lastY = coords[1];
			} else if(k==PathIterator.SEG_LINETO) {
				fail("this shape can not be simplified to a SEG_LINETO, but it is");
			} else if(k==PathIterator.SEG_QUADTO) {
				
				lastX = coords[2];
				lastY = coords[3];
			} else if(k==PathIterator.SEG_CUBICTO) {
				fail("this shape can be simplified to a SEG_QUADTO, but it isn't");
			}
			i.next();
		}*/
	}
}
