/*
 * @(#)RectangleReaderTest.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import junit.framework.TestCase;

public class RectangleReaderTest extends TestCase {
	
	public Shape[] getRectangles() {
		String[] strings = new String[] {
				"m 0 0 l 100 0 l 100 100 l 0 100",
				"m 100 0 l 100 100 l 0 100 l 0 0",
				"m 100 100 l 0 100 l 0 0 l 100 0",
				"m 0 100 l 0 0 l 100 0 l 100 100",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 0 0",
				"m 100 0 l 100 100 l 0 100 l 0 0 l 100 0",
				"m 100 100 l 0 100 l 0 0 l 100 0 l 100 100",
				"m 0 100 l 0 0 l 100 0 l 100 100 l 0 100",
				"m 0 0 l 0 100 l 100 100 l 100 0",
				"m 100 0 l 0 0 l 0 100 l 100 100",
				"m 100 100 l 100 0 l 0 0 l 0 100",
				"m 0 100 l 100 100 l 100 0 l 0 0",
				"m 0 0 l 0 100 l 100 100 l 100 0 l 0 0",
				"m 100 0 l 0 0 l 0 100 l 100 100 l 100 0",
				"m 100 100 l 100 0 l 0 0 l 0 100 l 100 100",
				"m 0 100 l 100 100 l 100 0 l 0 0 l  0 100",
				"m 0 0 l 100 0 l 100 100 l 0 100 z",
				"m 100 0 l 100 100 l 0 100 l 0 0 z",
				"m 100 100 l 0 100 l 0 0 l 100 0 z",
				"m 0 100 l 0 0 l 100 0 l 100 100 z",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 0 0 z",
				"m 100 0 l 100 100 l 0 100 l 0 0 l 100 0 z",
				"m 100 100 l 0 100 l 0 0 l 100 0 l 100 100 z",
				"m 0 100 l 0 0 l 100 0 l 100 100 l 0 100 z",
				"m 0 0 l 0 100 l 100 100 l 100 0 z",
				"m 100 0 l 0 0 l 0 100 l 100 100 z",
				"m 100 100 l 100 0 l 0 0 l 0 100 z",
				"m 0 100 l 100 100 l 100 0 l 0 0 z",
				"m 0 0 l 0 100 l 100 100 l 100 0 l 0 0 z",
				"m 100 0 l 0 0 l 0 100 l 100 100 l 100 0 z",
				"m 100 100 l 100 0 l 0 0 l 0 100 l 100 100 z",
				"m 0 100 l 100 100 l 100 0 l 0 0 l  0 100 z",
				"m 50 0 l 100 0 l 100 100 l 0 100 l 0 0 l 50 0",
				"m 50 0 l 100 0 l 100 100 l 0 100 l 0 0 z",
		};
		Shape[] shape = new Shape[strings.length];
		for(int a = 0; a<strings.length; a++) {
			shape[a] = ShapeStringUtils.createGeneralPath(strings[a]);
		}
		return shape;
	}
	
	public Shape[] getNonRectangles() {
		String[] strings = new String[] {
				"m 0 0 l 100 0 l 100 100",
				"m 50 0 l 100 100 l 0 100",
				"m 0 0 l 100 0 l 100 100 z",
				"m 50 0 l 100 100 l 0 100 z",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 0 75 l 100 75 l 100 25",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 0 75 l 100 75 l 100 25 l 0 25",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 0 75 l 100 75 l 100 25 l 0 25 z",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 100 100",
				"m 0 0 l 100 0 l 100 100 l 0 100 l 100 0"
		};
		Shape[] shape = new Shape[strings.length];
		for(int a = 0; a<strings.length; a++) {
			shape[a] = ShapeStringUtils.createGeneralPath(strings[a]);
		}
		return shape;
	}
	
	public void testConvert() {
		AffineTransform[] goodTransforms = new AffineTransform[] {
				new AffineTransform(),
				new AffineTransform(2, 0, 0, 2, 0, 0),
				new AffineTransform(2, 0, 0, 2, -100, -100),
				new AffineTransform(.2, 0, 0, .2, 0, 0),
				new AffineTransform(.2, 0, 0, .2, -100, -100),
				new AffineTransform(0, -1, 1, 0, 0, 0),
				new AffineTransform(0, 1, -1, 0, -100, -100)
		};
		AffineTransform[] badTransforms = new AffineTransform[] {
				TransformUtils.createAffineTransform(
					0, 0, 100, 100, 100, 0,
					0, 0, 100, 100, 75, 75
				),
				TransformUtils.createAffineTransform(
						0, 100, 100, 0, 0, 0,
						0, 100, 100, 0, 25, 25
				),
				AffineTransform.getRotateInstance( 1 )
		};
		
		
		Shape[] shapes = getRectangles();
		for(int a = 0; a<shapes.length; a++) {
			for(int b = 0; b<goodTransforms.length; b++) {
				assertTrue("a = "+a+" b = "+b, RectangleReader.isRectangle(shapes[a], goodTransforms[b]) );
			}
			for(int b = 0; b<badTransforms.length; b++) {
				assertFalse("a = "+a+" b = "+b, RectangleReader.isRectangle(shapes[a], badTransforms[b]) );
			}
		}
		shapes = getNonRectangles();
		for(int a = 0; a<shapes.length; a++) {
			for(int b = 0; b<goodTransforms.length; b++) {
				assertFalse("a = "+a+" b = "+b, RectangleReader.isRectangle(shapes[a], goodTransforms[b]) );
			}
			for(int b = 0; b<badTransforms.length; b++) {
				assertFalse("a = "+a+" b = "+b, RectangleReader.isRectangle(shapes[a], badTransforms[b]) );
			}
		}
	}
}
