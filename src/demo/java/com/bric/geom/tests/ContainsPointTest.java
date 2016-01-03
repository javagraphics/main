/*
 * @(#)ContainsPointTest.java
 *
 * $Date: 2014-05-07 01:28:23 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.geom.tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.bric.geom.MutablePath;
import com.bric.geom.RectangularTransform;
import com.bric.util.BooleanProperty;
import com.bric.util.EnumProperty;
import com.bric.util.IntProperty;
import com.bric.util.Property;

public class ContainsPointTest extends Test {

	GeneralPath path = new GeneralPath();
	Random random = new Random();
	Point2D point = new Point2D.Double();
	Shape shape = null;
	
	Property segmentType = new EnumProperty( "Segment Type",new String[] {"All", "Lines", "Quads", "Cubics"}, "All");
	IntProperty pathCount = new IntProperty("Path Count",1,50,1);
	IntProperty segmentCount = new IntProperty("Segment Count",2,50,5);
	BooleanProperty closing = new BooleanProperty("Closing Shapes",true);
	Property shapeType = new EnumProperty("Shape Type", new String[] {"GeneralPath", "MutablePath", "Area"}, "GeneralPath");
	
	boolean reallyContains;
	
	@Override
	public Property[] getProperties() {
		return new Property[] {
				segmentType, segmentCount, closing, pathCount, shapeType, getCompletion(), getSuccessRate()
		};
	}
	
	@Override
	protected int getCaseCount() {
		return 1000000;
	}

	@Override
	protected void prepCase(int caseIndex) {
		int pathIndex = caseIndex/1000;
		int pointIndex = caseIndex%1000;
		random.setSeed(pathIndex*1000);
		path.reset();
		path.moveTo( 100*random.nextFloat(), 100*random.nextFloat());
		for(int b = 0; b<pathCount.getValue(); b++) {
			for(int a = 0; a<segmentCount.getValue(); a++) {
				if(segmentType.getValue().equals("All")) {
					int type = random.nextInt(3);
					if(type==0) {
						path.lineTo( 100*random.nextFloat(), 100*random.nextFloat() );
					} else if(type==1) {
						path.quadTo( 100*random.nextFloat(), 100*random.nextFloat(),
								100*random.nextFloat(), 100*random.nextFloat() );
					} else {
						path.curveTo( 100*random.nextFloat(), 100*random.nextFloat(),
								100*random.nextFloat(), 100*random.nextFloat(),
								100*random.nextFloat(), 100*random.nextFloat() );
					}
				} else if(segmentType.getValue().equals("All")) {
					path.lineTo( 100*random.nextFloat(), 100*random.nextFloat() );
				} else if(segmentType.getValue().equals("Quads")) {
					path.quadTo( 100*random.nextFloat(), 100*random.nextFloat(),
							100*random.nextFloat(), 100*random.nextFloat() );
				} else {
					path.curveTo( 100*random.nextFloat(), 100*random.nextFloat(),
							100*random.nextFloat(), 100*random.nextFloat(),
							100*random.nextFloat(), 100*random.nextFloat() );
				}
			}
			if(closing.getValue()) {
				path.closePath();
			}
		}
		
		if(shapeType.getValue().equals("GeneralPath")) {
			shape = path;
		} else if(shapeType.getValue().equals("MutablePath")) {
			shape = new MutablePath(path);
		} else if(shapeType.getValue().equals("Area")) {
			shape = new Area(path);
		}

		random.setSeed(pointIndex*1000);
		point.setLocation(100*random.nextDouble(), 100*random.nextDouble() );
		
		reallyContains = runGraphicalContainsTest(shape, point);
	}

	static BufferedImage image = new BufferedImage(3,3,BufferedImage.TYPE_INT_ARGB);
	static RectangularTransform transform = new RectangularTransform();
	public synchronized static boolean runGraphicalContainsTest(Shape shape,Point2D p) {	
		double k = .0000001;
		transform.setTransform(
			new Rectangle2D.Double(p.getX()-k,p.getY()-k,2*k,2*k),
			new Rectangle(1,1,1,1)
		);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,image.getWidth(),image.getHeight());
		g.setTransform(transform.createAffineTransform());
		g.setColor(Color.black);
		g.fill(shape);
		g.dispose();
		int rgb = image.getRGB(1, 1);
		image.flush();
		
		if((rgb & 0x0000ff)==0) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getName() {
		return "contains(Point2D)";
	}

	@Override
	protected boolean runCase(int caseIndex) {
		boolean contains = false;
		for(int a = 0; a<1000; a++) {
			contains = shape.contains(point);
		}
		return contains==reallyContains;
	}


}
