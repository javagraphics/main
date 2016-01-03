/*
 * @(#)AngleSliderUIDemoHelper.java
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class AngleSliderUIDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		JSlider slider1 = new JSlider();
		JSlider slider2 = new JSlider();
		AngleSliderUI ui1 = new AngleSliderUI();
		AngleSliderUI ui2 = new AquaAngleSliderUI();
		slider1.setUI(ui1);
		slider1.setSize(slider1.getPreferredSize());
		
		slider2.setUI(ui2);
		slider2.setSize(slider2.getPreferredSize());
		
		final JFrame f = new JFrame();
		f.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(2,2,2,2);
		f.getContentPane().setBackground(Color.white);
		f.getContentPane().add(slider1,c);
		c.gridx++;
		f.getContentPane().add(slider2,c);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				f.pack();
			}
		});
		
		BufferedImage image = new BufferedImage(f.getContentPane().getWidth(), f.getContentPane().getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		f.getContentPane().paint(g);
		g.dispose();
		return image;
	}

	/** Creates a file named "angleslider.gif" in the directory provided.
	 * 
	 * @param robot the Robot to use to create this demo.
	 * @param directory the directory to write this resource in.
	 * @return the file "angleslider.gif"
	 * 
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "angleslider.gif");
		ResettableAnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static ResettableAnimationReader createAnimation(Robot robot) throws Exception {
		final AngleSliderUIDemo asd = new AngleSliderUIDemo();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.getContentPane().add(asd);
				asd.enabled1.setVisible(false);
				asd.enabled2.setVisible(false);
				asd.label1.setVisible(false);
				asd.label2.setVisible(false);
				asd.slider1.setBorder(new EmptyBorder(20,20,20,20));
				asd.slider2.setBorder(new EmptyBorder(20,20,20,20));
				frame.pack();
				Dimension d = frame.getPreferredSize();
				frame.setSize(d);
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
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(asd);
		screenCapture.setPlaybackRate(1);
		screenCapture.setTargetedRepaints(false);
		JSlider slider1 = asd.slider1;
		JSlider slider2 = asd.slider2;

		animateMouse(robot, slider1, false, false);
		Thread.sleep(1000);
		screenCapture.start();
		circle(robot, slider1, slider1.getHeight()/3);
		Thread.sleep(2000);
		animateMouse(robot, slider2, true, false);
		circle(robot, slider2, slider1.getHeight()/3);
		Thread.sleep(2000);
		ResettableAnimationReader returnValue = screenCapture.stop(true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.setVisible(false);
			}
		});
	
		return returnValue;
	}
	
	private static void circle(Robot robot, JComponent jc,int radius) {
		Point p = new Point(jc.getWidth()/2, jc.getHeight()/2);
		SwingUtilities.convertPointToScreen(p, jc);
		robot.mouseMove(p.x-3, p.y);
		

		robot.mousePress(InputEvent.BUTTON1_MASK);
		beat(200*4);
		
		for(int r = 3; r<radius; r++) {
			robot.mouseMove(p.x-r, p.y);
			beat(50);
		}
		for(double theta = 0; theta<2*Math.PI; theta+=.04) {
			robot.mouseMove(
					(int)(p.x+radius*Math.cos(theta+Math.PI)), 
					(int)(p.y+radius*Math.sin(theta+Math.PI)) );
			beat(50);
		}

		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		beat(200*4);
	}
}
