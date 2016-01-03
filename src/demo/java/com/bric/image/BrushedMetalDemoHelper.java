/*
 * @(#)BrushedMetalDemoHelper.java
 *
 * $Date$
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
package com.bric.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;

public class BrushedMetalDemoHelper extends BlogHelper {


	/** Create a blurb graphic for this demo app. */
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		Line2D shape = new Line2D.Float(100,100,200,125);
		return BrushedMetalLook.paint(shape, 20, null, Color.gray, true);
	}
}
