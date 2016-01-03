/*
 * @(#)QuantizationDemoHelper.java
 *
 * $Date: 2014-05-07 01:26:55 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.image.pixel.quantize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.SwingConstants;

import com.bric.blog.BlogHelper;
import com.bric.image.ImageLoader;
import com.bric.image.pixel.Scaling;
import com.bric.swing.resources.ArrowIcon;

/** Something to help generate graphics.
 */
public class QuantizationDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		int width = preferredSize.width*1/3;
		int height = preferredSize.height-5;
		Icon arrowIcon = new ArrowIcon(SwingConstants.EAST, 16, 16);
		BufferedImage bigImage = ImageLoader.createImage( ImageLoader.class.getResource("thumbnail/swing/resources/sample.jpg") );
		BufferedImage origImage = Scaling.scaleProportionally(bigImage, new Dimension(width, height));
		BufferedImage reducedImage = ImageQuantization.reduce(origImage, 16);
		
		BufferedImage combined = new BufferedImage(width*2 + arrowIcon.getIconWidth()+4, origImage.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g = combined.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,combined.getWidth(),combined.getHeight());
		g.drawImage(origImage, 0, 0, null);
		arrowIcon.paintIcon(null, g, origImage.getWidth()+2, combined.getHeight()/2 - arrowIcon.getIconHeight()/2);
		g.drawImage(reducedImage, origImage.getWidth()+arrowIcon.getIconWidth()+4, 0, null);
		g.dispose();
		return combined;
	}
}
