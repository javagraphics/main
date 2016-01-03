/*
 * @(#)WritingStrokeTest.java
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
 * http://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.animation.writing;

import java.awt.geom.GeneralPath;
import java.util.Random;

import junit.framework.TestCase;

public class WritingStrokeTest extends TestCase {
	public void testToString() {
		for(int seed = 0; seed<1000; seed++) {
			WritingStroke ws1 = createStroke(new Random(seed));
			WritingStroke ws2 = new WritingStroke(ws1.toString());
			assertEquals(ws1.toString(), ws2.toString() );
			assertEquals(ws1, ws2 );
		}
	}
	
	private WritingStroke createStroke(Random rs) {
		GeneralPath shape = new GeneralPath();
		shape.moveTo(rs.nextFloat()*100, rs.nextFloat()*100);
		shape.lineTo(rs.nextFloat()*100, rs.nextFloat()*100);
		shape.quadTo(rs.nextFloat()*100, rs.nextFloat()*100,
				rs.nextFloat()*100, rs.nextFloat()*100);
		shape.curveTo(rs.nextFloat()*100, rs.nextFloat()*100,
				rs.nextFloat()*100, rs.nextFloat()*100,
				rs.nextFloat()*100, rs.nextFloat()*100 );
		if(rs.nextBoolean()) {
			shape.closePath();
		}
		
		WritingStroke ws = new WritingStroke(rs.nextFloat()*100, shape);
		return ws;
	}
}
