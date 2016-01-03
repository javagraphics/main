/*
 * @(#)ColorPickerDemoHelper.java
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
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.BlogHelper;

public class ColorPickerDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		ColorPicker picker = new ColorPicker();
		picker.setColor(new Color(0x4F63C3));
		JFrame frame = new JFrame();
		final JInternalFrame window = new JInternalFrame();
		window.getContentPane().add(picker);
		window.pack();
		frame.getContentPane().add(window);
		frame.pack();
		
		final BufferedImage image = new BufferedImage(window.getWidth(), window.getHeight(), BufferedImage.TYPE_INT_ARGB);

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				Graphics2D g = image.createGraphics();
				window.paint(g);
				g.dispose();
			}
		});
		
		return image;
	}
}
