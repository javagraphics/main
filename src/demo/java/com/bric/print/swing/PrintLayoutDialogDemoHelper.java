/*
 * @(#)PrintLayoutDialogHelper.java
 *
 * $Date: 2014-05-07 01:16:47 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.print.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.awt.DemoPaintable;
import com.bric.awt.Paintable;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.print.PrintLayout;

public class PrintLayoutDialogDemoHelper {


	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		final JFrame frame = new JFrame();
		final PrintLayoutDialog dialog = new PrintLayoutDialog(frame, "Print" , new PrintLayout(),
				new Paintable[] { new DemoPaintable(240,320,"A") }, null);
		dialog.setModal(false);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		BufferedImage image = new BufferedImage(dialog.getWidth(), dialog.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		dialog.getContentPane().paint(g);
		g.dispose();
		dialog.setVisible(false);
		return BasicThumbnail.getShadow(3).create(image, preferredSize);
	}
}
