/*
 * @(#)ScalingDemoHelper.java
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
package com.bric.image.pixel;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import com.bric.blog.BlogHelper;

public class ScalingDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		BufferedImage img1 = ImageIO.read(new URL("https://javagraphics.java.net/resources/scaling_compare2.png"));
		Dimension d = Scaling.scaleDimensionsProportionally(new Dimension(img1.getWidth(),img1.getHeight()),preferredSize);
		//actually in this case we want to *avoid* using antialiased scaling,
		//so the graphic (which is a close up of pixels) remains pixelated.
		BufferedImage img2 = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img2.createGraphics();
		g.scale( ((double)img2.getWidth())/((double)img1.getWidth()), 
				((double)img2.getHeight())/((double)img1.getHeight()) );
		g.drawImage(img1, 0, 0, null);
		g.dispose();
		return img2;
	}
}
