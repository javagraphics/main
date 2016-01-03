/*
 * @(#)Transition3DDemoHelper.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
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
package com.bric.image.transition;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;

public class Transition3DDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		BufferedImage frameA = AbstractTransition.createImage(400,"A",true,true);
		BufferedImage frameB = AbstractTransition.createImage(400,"B",false,true);
		BufferedImage finalImage = new BufferedImage(400,400,BufferedImage.TYPE_INT_ARGB);
		Transition transition = new CubeTransition3D(Transition.LEFT, true, Color.WHITE);
		float fraction = .55f;
		Graphics2D g = finalImage.createGraphics();
		g.setRenderingHints(Transition2DDemo.createQualityHints());
		transition.paint(g, frameA, frameB, fraction);
		g.dispose();
		return finalImage;
	}
}
