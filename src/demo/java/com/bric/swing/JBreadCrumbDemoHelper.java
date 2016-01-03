/*
 * @(#)JBreadCrumbDemoHelper.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.bric.animation.AnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.RobotScreenCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;
import com.bric.plaf.BreadCrumbUI;

public class JBreadCrumbDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		final JBreadCrumbDemo jbcd = new JBreadCrumbDemo(0, false, "", "", "Crumbs");
		final Dimension d = jbcd.getPreferredSize();

		final BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					jbcd.crumbs2Container.setVisible(false);
					jbcd.crumbs1.setSize(d);
					jbcd.crumbs1.getLayout().layoutContainer(jbcd.crumbs1);

					Graphics2D g = bi.createGraphics();
					g.setColor(Color.white);
					g.fillRect(0,0,bi.getWidth(),bi.getHeight());
					jbcd.crumbs1.paint(g);
					g.dispose();
				}
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		return bi;
	}
	
	/** Creates a file named "breadcrumbs.gif" in the directory provided. 
	 * 
	 * @param robot the Robot used to create this demo
	 * @param directory the directory this resource is stored in
	 * @return a file titled "breadcrumbs.gif"
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "breadcrumbs.gif");

		AnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static AnimationReader createAnimation(Robot robot) throws Exception {
		final JBreadCrumbDemo jbcd = new JBreadCrumbDemo(5, false, "Bread", "Crumbs", "Demo");
		jbcd.crumbs1.setUI(new BreadCrumbUI(true));
		final Dimension d = jbcd.getPreferredSize();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				jbcd.crumbs2Container.setVisible(false);
				jbcd.crumbs1.setBorder(new EmptyBorder(5,15,15,15));
				frame.getContentPane().add(jbcd);
				frame.pack();
				Dimension d = frame.getPreferredSize();
				d.width -= 50;
				frame.setSize(d);;
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		Rectangle bounds = frame.getBounds();
		Insets i = frame.getInsets();
		bounds.x += i.left;
		bounds.y += i.top;
		bounds.width -= i.left + i.right;
		bounds.height -= i.top + i.bottom + 3;
		
		RobotScreenCapture screenCapture = new RobotScreenCapture(bounds);
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(2);

		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(1000);
		
		for(int x = d.width+30; x>=20; x--) {
			Point p = new Point(x, jbcd.crumbs1.getHeight()-20);
			SwingUtilities.convertPointToScreen(p, jbcd.crumbs1);
			robot.mouseMove(p.x, p.y);
			Thread.sleep(100);
		}

		for(int x = 20; x<=d.width+30; x++) {
			Point p = new Point(x, jbcd.crumbs1.getHeight()-20);
			SwingUtilities.convertPointToScreen(p, jbcd.crumbs1);
			robot.mouseMove(p.x, p.y);
			Thread.sleep(100);
		}
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch(Throwable t) {
					t.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		
		return screenCapture.stop(true);
	}
}
