/*
 * @(#)InspectorTestHelper.java
 *
 * $Date: 2014-05-06 21:37:08 +0200 (Di, 06 Mai 2014) $
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
package com.bric.inspector;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.BlogHelper;

import com.bric.blog.BlogHelper;

public class InspectorTestHelper extends BlogHelper {
	
	public static BufferedImage createBlurbGraphic(Dimension preferredMaxSize) throws Throwable {
		InspectorTest test = new InspectorTest();
		test.setSize(test.getPreferredSize());
		final JFrame frame = new JFrame();
		frame.getContentPane().add(test);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.pack();
				//again: I don't know why this is necessary.
				//but I'm in a hurry and this only affects the BlogUpdaterApp anyway...
				frame.setVisible(true);
			}
		});
		
		BufferedImage image = new BufferedImage( test.getWidth(), test.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		test.paint(g);
		g.dispose();
		frame.setVisible(false);
		return image;
	}
}
