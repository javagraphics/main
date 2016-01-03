/*
 * @(#)JThrobberDemoHelper.java
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
package com.bric.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;
import com.bric.plaf.AquaThrobberUI;
import com.bric.plaf.ChasingArrowsThrobberUI;
import com.bric.plaf.PulsingCirclesThrobberUI;
import com.bric.plaf.ThrobberUI;

public class JThrobberDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		
		BufferedImage bi = new BufferedImage(3*40, 40, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		Dimension d = new Dimension(32, 32);
		(new AquaThrobberUI()).createIcon(.25f, d).paintIcon(null, g, 4, 4);
		(new ChasingArrowsThrobberUI()).createIcon(.25f, d).paintIcon(null, g, 4+40, 4);
		(new PulsingCirclesThrobberUI()).createIcon(.25f, d).paintIcon(null, g, 4+80, 4);
		g.dispose();		

		return bi;
	}
	
	/** Creates a file named "throbber.gif" in the directory provided.
	 * 
	 * @param robot the robot to use to create this demo
	 * @param directory the directory to write this resource in.
	 * @return the file "throbber.gif"
	 * 
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createScreenCapture(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "throbber.gif");
		
		AnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}

	private static AnimationReader createAnimation(Robot robot) throws Exception {
		final JThrobberDemo td = new JThrobberDemo();
		final Dimension d = td.getPreferredSize();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				td.vectorZoom.doClick();
				td.aquaIndicator.setVisible(false);
				frame.getContentPane().add(td);
				frame.pack();
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
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(td.getContentPane());
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(4);
		td.setSlowFactor(4);

		Thread.sleep(1000);
		screenCapture.start();
		BlogHelper.animateMouse(robot, td.throbbers.get(0), false, false);
		Thread.sleep(1000);
		
		Point center = new Point(td.throbberContainer.getWidth()/2, td.throbberContainer.getHeight()/2);
		SwingUtilities.convertPointToScreen(center, td.throbberContainer);

		for(JComponent jc : td.throbbers) {
			BlogHelper.animateMouse(robot, jc, true, false);
			Thread.sleep(4000);
		}
		BlogHelper.animateMouse(robot, td.throbbers.get(0), true, false);
		
		SwingUtilities.invokeLater(new Runnable() {
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
	
	/** Creates a directory named "throbber" in the directory provided.
	 * 
	 * @param directory the directory meant to contain all resources/subdirectories.
	 * @return a new directory titled "throbber"
	 * @throws Exception if an error occurred creating these demo files. 
	 */
	public static File createIndividualDemos(File directory) throws Exception {
		File sampleDir = new File(directory, "throbber");
		if(!sampleDir.mkdirs())
			throw new RuntimeException("mkdirs failed for "+sampleDir.getAbsolutePath());
		
		Class<?>[] throbbers = getNonAbstractSubclasses(ThrobberUI.class, "com.bric");
		for(Class<?> c : throbbers) {
			ThrobberUI ui = (ThrobberUI)c.newInstance();
			String name = ui.getClass().getName();
			name = name.substring(name.lastIndexOf('.')+1);
			writeAnimation(ui, new File(sampleDir, name+".gif"), 1);
			writeAnimation(ui, new File(sampleDir, name+"x2.gif"), 2);
			writeAnimation(ui, new File(sampleDir, name+"x4.gif"), 4);
		}

		return sampleDir;
	}
	
	private static void writeAnimation(final ThrobberUI ui,File gifFile,int multiplier) throws IOException {
		Dimension d = ui.getPreferredSize();

		final int width = d.width*multiplier;
		final int height = d.height*multiplier;
		ResettableAnimationReader animation = new ResettableAnimationReader() {
			int frameCtr = 0;
			int frameCount = 100;
			
			BufferedImage bi = null;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(frameCtr==frameCount) return null;
				
				if(bi==null || cloneImage) {
					bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				}
				Graphics2D g = bi.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0,0,width,height);
				g.setComposite(AlphaComposite.SrcOver);
				Float fraction  = ((float)frameCtr)/((float)frameCount);
				ui.createIcon(fraction, new Dimension(width, height)).paintIcon(null, g, 0, 0);
				g.dispose();
				
				frameCtr++;
				
				return bi;
			}
			public double getDuration() {
				return getFrameDuration()*getFrameCount();
			}
			public int getFrameCount() {
				return frameCount;
			}
			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}
			public double getFrameDuration() {
				double period = 500;
				try {
					period = ((Number)ui.getClass().getField("DEFAULT_PERIOD").get(null)).intValue();
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
				return period/(1000.0*getFrameCount());
			}
			public int getWidth() {
				return width;
			}
			public int getHeight() {
				return height;
			}
			public void reset() {
				frameCtr = 0;
			}
		};
		GifWriter.write(gifFile, animation, ColorReduction.FROM_ALL_FRAMES);
	}
}
