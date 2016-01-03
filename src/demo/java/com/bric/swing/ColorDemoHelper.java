/*
 * @(#)ColorDemoHelper.java
 *
 * $Date: 2014-05-07 01:18:06 +0200 (Mi, 07 Mai 2014) $
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.BlogHelper;

import com.bric.blog.BlogHelper;

public class ColorDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		final BufferedImage[] returnValue = new BufferedImage[1];
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ColorDemo demo = new ColorDemo();
				demo.well.setColor(new Color(0x4F63C3));
				demo.setSize(demo.getPreferredSize());
				JFrame frame = new JFrame();
				frame.getContentPane().add(demo);
				frame.pack();
				//seriously?  I tried all sorts of other tricks,
				//but this is the only thing that lets it paint correctly:
				frame.setVisible(true);
				
				BufferedImage image = new BufferedImage(demo.getWidth(), demo.getHeight(), BufferedImage.TYPE_INT_ARGB);

				Graphics2D g = image.createGraphics();
				demo.paint(g);
				g.dispose();
				frame.setVisible(false);
				frame.dispose();
				
				returnValue[0] = image;
			}
		});
		
		return returnValue[0];
	}
}
