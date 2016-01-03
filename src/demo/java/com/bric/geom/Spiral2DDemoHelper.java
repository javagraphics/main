/*
 * @(#)Spiral2DDemoHelper.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.geom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.animation.AnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class Spiral2DDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		int size = Math.min(preferredSize.width, preferredSize.height);
		
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		Spiral2D s = Spiral2D.createWithFixedCoilGap(size/2, size/2, size/2,5,10);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.black);
		g.rotate(2, size/2, size/2);
		g.draw(s);
		g.dispose();
		return image;
	}
	
	/** Creates a file named "spiraldemo.gif" in the directory provided. 
	 * 
	 * @param robot the Robot to use to create this sample file.
	 * @param directory the master directory to store all resources/subdirectories in.
	 * @return the file titled "spiraldemo.gif"
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "spiraldemo.gif");

		AnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static AnimationReader createAnimation(Robot robot) throws Exception {
		final Spiral2DDemo sd = new Spiral2DDemo();
		final Dimension d = sd.getPreferredSize();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.getContentPane().add(sd);
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
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(sd);
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(1);

		Point p = new Point(sd.preview.getWidth()/2, sd.preview.getHeight()/2);
		SwingUtilities.convertPointToScreen(p, sd.preview);
		p.x += 10;
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(1000);
		
		dragOutAndIn(robot, p, true, sd);
		//beat(1500);
		//robot.keyPress(KeyEvent.VK_SHIFT);
		//dragOutAndIn(robot, p, true, sd);
		//robot.keyRelease(KeyEvent.VK_SHIFT);
		//beat(1500);
		BlogHelper.animateMouse(robot, sd.showControlPoints, true, true);
		BlogHelper.animateMouse(robot, sd.preview, true, false);
		beat(100);
		robot.mouseMove(p.x, p.y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		beat(100);
		dragOutAndIn(robot, p, false, sd);
		beat(100);
		BlogHelper.animateMouse(robot, sd.coilOffset, true, true);
		for(int a = 0; a<100; a++) {
			beat(50);
			robot.keyPress(KeyEvent.VK_UP);
			robot.keyRelease(KeyEvent.VK_DOWN);
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

	private static void dragOutAndIn(Robot robot,Point p,boolean returnMouse,Spiral2DDemo sd) {
		robot.mousePress(InputEvent.BUTTON1_MASK);
		for(int dx = 0; dx<100; dx++) {
			robot.mouseMove(p.x+dx, p.y);
			beat(20);
		}
		beat(500);
		if(returnMouse) {
			for(int dx = 100; dx>=0; dx--) {
				robot.mouseMove(p.x+dx, p.y);
				beat(20);
			}
		}
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
}
