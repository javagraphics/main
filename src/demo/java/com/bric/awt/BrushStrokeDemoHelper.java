/*
 * @(#)BrushStrokeDemoHelper.java
 *
 * $Date: 2014-05-07 01:04:50 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.awt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BrushStrokeDemoHelper extends StrokeHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {

		GeneralPath wiggle = new GeneralPath();
		Rectangle2D bounds = BrushStrokeDemo.defineWiggle(wiggle);

		BufferedImage image = new BufferedImage(preferredSize.width, (int)(preferredSize.height*.7), BufferedImage.TYPE_INT_ARGB);

		double scaleX = (image.getWidth()-15)/(bounds.getWidth());
		double scaleY = (image.getHeight()-15)/(bounds.getHeight());
		double translateX = -bounds.getX()*scaleX+image.getWidth()/2-(image.getWidth()-15)/2;
	    double translateY = -bounds.getY()*scaleY+image.getHeight()/2-(image.getHeight()-15)/2;
	    AffineTransform transform = new AffineTransform(scaleX, 0, 0, scaleY, translateX, translateY);
	    wiggle.transform(transform);
	    
		Graphics2D g = image.createGraphics();
		g.setStroke(new BrushStroke(10,.5f));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.black);
	    g.draw(wiggle);
		
		g.dispose();
		
		return image;
	}
	
	public static File createBrushSamples(File resourcesDir) throws IOException, NoSuchMethodException {
		File sampleDir = getSampleDir(resourcesDir);
		File linearSample = new File(sampleDir, "brushLines.png");
		File wavySample = new File(sampleDir, "brushWaves.gif");
		createWavySamples( wavySample, BrushStroke.class.getConstructor(
				new Class[] {Float.TYPE, Float.TYPE}), 
				"Thickness",
				new float[] {1, 3, 6, 10});
		createLinearSample( linearSample, 
				new BrushStroke(1, .5f),
				new BrushStroke(5, .65f),
				new BrushStroke(10, .8f)
				);
		return sampleDir;
	}
}
