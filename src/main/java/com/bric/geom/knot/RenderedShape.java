/*
 * @(#)RenderedShape.java
 *
 * $Date: 2014-03-21 08:38:53 +0100 (Fr, 21 Mär 2014) $
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
package com.bric.geom.knot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import com.bric.geom.ShapeBounds;
import com.bric.geom.intersect.IntersectionIdentifier;
import com.bric.geom.intersect.IntersectionIdentifier.SimpleIntersectionListener;
import com.bric.image.BrushedMetalLook;

public class RenderedShape {
	static int strokeWidth = 20;
	static BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	Shape shape;
	BufferedImage image;
	Rectangle imageBounds;
	Area body;
	int dx = 0;
	int dy = 0;
	
	public RenderedShape(Shape shape,Color color) {
		this.shape = shape;
		imageBounds = ShapeBounds.getBounds(shape).getBounds();
		imageBounds.x -= strokeWidth/2 + 3;
		imageBounds.y -= strokeWidth/2 + 3;
		imageBounds.width += strokeWidth + 6;
		imageBounds.height += strokeWidth + 6;
		image = BrushedMetalLook.paint(shape, strokeWidth, imageBounds, color, true);
		body = new Area( stroke.createStrokedShape(shape) );
	}
	
	public boolean contains(int x,int y) {
		return body.contains(x - dx, y - dy);
	}
	
	public void paint(Graphics g) {
		g.drawImage(image,dx+imageBounds.x, dy+imageBounds.y,null);
	}
	
	public void paint(Graphics g,double x,double y,RenderedShape otherShape,boolean debug) {
		Graphics2D g2 = (Graphics2D)g.create();
		Area clip = new Area(body);
		clip.transform(AffineTransform.getTranslateInstance(dx, dy));
		clip.intersect(new Area(new Ellipse2D.Double(x - strokeWidth*3/2, y - strokeWidth*3/2, strokeWidth*3, strokeWidth*3)));
		
		Area otherBody = new Area(otherShape.body);
		otherBody.transform(AffineTransform.getTranslateInstance(otherShape.dx, otherShape.dy));
		clip.intersect(otherBody);
		
		g2.clip(clip);
		if(debug) {
			g2.setStroke(new BasicStroke(5));
			g2.setColor(Color.green);
			g2.draw(clip);
		}
		g2.translate(dx, dy);
		if(debug) {
			g2.setColor(Color.cyan);
			g2.draw(body);
		}
		g2.translate(imageBounds.x, imageBounds.y);
		if(debug) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f));
		}
		g2.drawImage(image,0,0,null);
		g2.dispose();
	}
	
	public void getIntersections(final RenderedShape other,final Collection<Intersection> dest) {
		AffineTransform myTranslation = AffineTransform.getTranslateInstance(dx, dy);
		AffineTransform otherTranslation = AffineTransform.getTranslateInstance(other.dx, other.dy);
		IntersectionIdentifier.get().getIntersections( 
				shape, myTranslation, 
				other.shape, otherTranslation,
				new SimpleIntersectionListener() {

					@Override
					public void intersection(double x, double y, double t1,
							double t2, int segmentIndex1, int segmentIndex2) {
						dest.add(new Intersection(
								RenderedShape.this,
								other,
								x, y,
								segmentIndex1, t1,
								segmentIndex2, t2
							));
						
					}
					
				});
		
	}
}
