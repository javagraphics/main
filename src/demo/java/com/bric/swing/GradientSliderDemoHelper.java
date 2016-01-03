/*
 * @(#)GradientSliderDemoHelper.java
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
package com.bric.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import com.bric.blog.BlogHelper;

public class GradientSliderDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredMaxSize) throws Exception {
		final GradientSlider slider = new GradientSlider();
		slider.setValues(new float[] {0, .7f, 1}, new Color[] { Color.red, new Color(0xDD6611), Color.black});
		slider.setPaintTicks(true);
		slider.putClientProperty("MultiThumbSlider.indicateComponent", "false");
		slider.setSize(slider.getPreferredSize());
		final BufferedImage image = new BufferedImage(slider.getWidth(), slider.getHeight(), BufferedImage.TYPE_INT_ARGB);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				Graphics2D g = image.createGraphics();
				slider.paint(g);
				g.dispose();
				//because the thumbs will fade in, we need to give this time:
				try {
					Thread.sleep(1000);
				} catch(Exception e) {
					e.printStackTrace();
				}
				//The thumbs should paint now:
				g = image.createGraphics();
				g.setComposite(AlphaComposite.Clear);
				g.fillRect(0,0,image.getWidth(),image.getHeight());
				g.setComposite(AlphaComposite.SrcOver);
				slider.paint(g);
				g.dispose();
			}
		});
		return image;
	}
}
