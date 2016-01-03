/*
 * @(#)ClipperDemoHelper.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.geom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class ClipperDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		CurvedPolyline unclippedShape = new CurvedPolyline();
		
		unclippedShape.addPoint( preferredSize.width*4/5, preferredSize.height*1/9);
		unclippedShape.addPoint( preferredSize.width*1/5, preferredSize.height*5/7);
		unclippedShape.addPoint( preferredSize.width*2/3, preferredSize.height*5/6);
		unclippedShape.addPoint( preferredSize.width*3/4, preferredSize.height*1/11);
		
		Rectangle clippingRectangle = new Rectangle(
			(int)(preferredSize.width*1/5),
			(int)(preferredSize.height*1/5),
			(int)(preferredSize.width*3/5),
			(int)(preferredSize.height*3/5)
		);
		Shape clippedShape = Clipper.clipToRect(unclippedShape, clippingRectangle);
		
		BufferedImage bi = new BufferedImage(preferredSize.width, preferredSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,bi.getWidth(),bi.getHeight());
		g.setRenderingHints(qualityHints);
		g.setColor(new Color(0, 0, 255, 50));
		g.fill(unclippedShape);
		g.setColor(new Color(0, 80, 255, 250));
		g.fill(clippedShape);
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[] {2, 2}, 0));
		g.setRenderingHints(speedHints);
		g.draw(clippingRectangle);
		g.dispose();
		return bi;
	}

	/** Create "clipping.gif", which clips a portion of an ellipse. 
	 * @throws IOException if an IO problem occurs.
	 */
	public static File createSample1(File dir) throws IOException {
		File f = new File(dir, "clipping.gif");
		Ellipse2D simpleShape = new Ellipse2D.Float(10,10,90,90);
		Rectangle clipping = new Rectangle(20,20,70,70);
		AnimationReader r = createAnimation(simpleShape, clipping, 100);
		GifWriter.write(f, r, ColorReduction.FROM_ALL_FRAMES);
		return f;
	}

	/** Create "clipping2.gif", which clips a portion of a capital sigma glyph. 
	 * @throws IOException if an IO problem occurs.
	 */
	public static File createSample2(File dir) throws IOException {
		File f = new File(dir, "clipping2.gif");
		
		Font font = new Font("Verdana", 0, 100);
		char ch = '\u03A3';
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
		Shape sigma = font.createGlyphVector(frc, new char[] {ch}).getOutline();
		Rectangle clipping = sigma.getBounds().getBounds();
		clipping.x += 5;
		clipping.y += 5;
		clipping.width -= 10;
		clipping.height -= 10;
		AnimationReader r = createAnimation( new Area(sigma), clipping, 100);
		GifWriter.write(f, r, ColorReduction.FROM_ALL_FRAMES);
		return f;
	}
	
	private static AnimationReader createAnimation(final Shape shape,final Rectangle clipping,final int frameCount) {
		final MeasuredShape ms = MeasuredShape.getSubpaths(shape)[0];
		final Rectangle systemBounds = ShapeBounds.getBounds(shape).getBounds();
		systemBounds.add(clipping);
		systemBounds.x -= 2;
		systemBounds.y -= 2;
		systemBounds.width += 4;
		systemBounds.height += 4;
		
		ResettableAnimationReader r = new ResettableAnimationReader() {
			int frameIndex = 0;

			BufferedImage bi = null;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(frameIndex==frameCount) return null;
				if(bi==null || cloneImage) {
					bi = new BufferedImage(systemBounds.width, systemBounds.height, BufferedImage.TYPE_INT_RGB);
				}
				
				Graphics2D g = bi.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
				g.setRenderingHints(qualityHints);
				g.translate(-systemBounds.x, -systemBounds.y);
				g.setColor(new Color(220, 20, 250, 100));
				g.fill(shape);
				g.setColor(new Color(220, 20, 250, 150));
				g.fill( Clipper.clipToRect(shape, clipping) );
				g.setColor(Color.black);
				g.setRenderingHints(speedHints);
				g.draw(clipping);

				g.setRenderingHints(qualityHints);
				float f = ((float)frameIndex)/((float)frameCount-1f) * ms.getClosedDistance();
				Point2D p1 = ms.getPoint( f, null);
				g.setColor(Color.blue);
				g.fill(new Ellipse2D.Double(p1.getX()-2,p1.getY()-2,4,4));
				
				Point2D p2 = new Point2D.Double(
					Math.max( clipping.x, Math.min(clipping.x + clipping.width, p1.getX())),
					Math.max( clipping.y, Math.min(clipping.y + clipping.height, p1.getY()))	
				);
				if(!p1.equals(p2)) {
					g.setColor(Color.red);
					g.fill(new Ellipse2D.Double(p2.getX()-2,p2.getY()-2,4,4));
				}
				
				g.dispose();
				
				frameIndex++;
				return bi;
			}

			public double getDuration() {
				double sum = 0;
				for(int a = 0; a<frameCount; a++) {
					sum += getFrameDuration(a);
				}
				return sum;
			}

			public int getFrameCount() {
				return frameCount;
			}

			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}

			public double getFrameDuration() {
				return getFrameDuration(frameIndex-1);
			}
			
			private double getFrameDuration(int index) {
				if(index==0 || index==frameCount-1)
					return 2;
				return .1;
			}

			public int getWidth() {
				return systemBounds.width;
			}

			public int getHeight() {
				return systemBounds.height;
			}

			public void reset() {
				frameIndex = 0;
			}
			
		};
		return r;
	}
}
