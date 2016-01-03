/*
 * @(#)HalftoneGradientDemoHelper.java
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
import java.awt.Point;
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;

public class HalftoneGradientDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredMaxSize) {
		int min = Math.min(preferredMaxSize.width, preferredMaxSize.height);
		BufferedImage image = new BufferedImage( (preferredMaxSize.width+min)/2, (preferredMaxSize.height+min)/2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setPaint(new HalftoneGradient( new Point(0,image.getHeight()-min/3),
				Color.white,
				new Point(image.getWidth(),image.getHeight()),
				Color.orange));
		g.fillRect(0,0,image.getWidth(),image.getHeight());
		g.dispose();
		return image;
	}
}
