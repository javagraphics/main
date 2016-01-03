/*
 * @(#)ColorBandDemoHelper.java
 *
 * $Date: 2014-05-04 17:57:20 +0200 (So, 04 Mai 2014) $
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
package com.bric.awt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class ColorBandDemoHelper extends BlogHelper {
	
	public static File createResources(File dir) throws ClassNotFoundException, IOException, InvocationTargetException, InterruptedException {
		final File myDir = new File(dir, "colorband");
		if(!myDir.mkdirs())
			throw new RuntimeException("mkdirs failed for "+myDir.getAbsolutePath());
		
		createAnimation(1000, 200, new Color(130, 190, 220), new Color(130, 160, 180), true, false, new File(myDir, "defaultPaint.gif"));
		createAnimation(1000, 200, new Color(130, 190, 220), new Color(130, 160, 180), true, true, new File(myDir, "comparison.gif"));
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ColorBandDemo cbd = new ColorBandDemo();
				cbd.diffusedPaintLabel.setVisible(false);
				cbd.diffusedPaintZoomLabel.setVisible(false);
				cbd.controller.setVisible(false);
				cbd.controller.setTime(cbd.controller.getDuration()/4f);
				BufferedImage bi = BlogHelper.paint(cbd.getContentPane(), new Dimension(600, 100));
				try {
					ImageIO.write(bi, "png", new File(myDir, "bandDemo.png"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});

		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ColorBandDemo cbd = new ColorBandDemo();
				GradientTexturePaint.seedingEnabled = false;
				cbd.defaultPaintLabel.setVisible(false);
				cbd.defaultPaintZoomLabel.setVisible(false);
				cbd.controller.setVisible(false);
				cbd.controller.setTime(cbd.controller.getDuration()/4f);
				BufferedImage bi = BlogHelper.paint(cbd.getContentPane(), new Dimension(600, 100));
				try {
					ImageIO.write(bi, "png", new File(myDir, "simpleDiffusion.png"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				GradientTexturePaint.seedingEnabled = true;
			}
		});

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ColorBandDemo cbd = new ColorBandDemo();
				cbd.defaultPaintLabel.setVisible(false);
				cbd.defaultPaintZoomLabel.setVisible(false);
				cbd.controller.setVisible(false);
				cbd.controller.setTime(cbd.controller.getDuration()/4f);
				BufferedImage bi = BlogHelper.paint(cbd.getContentPane(), new Dimension(600, 100));
				try {
					ImageIO.write(bi, "png", new File(myDir, "seededDiffusion.png"));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		return dir;
	}
	
	private static void createAnimation(final int width,final int heightPerBar,
			final Color c1,
			final Color c2,
			final boolean includeDefaultPaint,
			final boolean includeDiffusedPaint,
			final File gifFile) throws IOException {
		
		final int barCount;
		if(includeDefaultPaint && includeDiffusedPaint) {
			barCount = 2;
		} else if(includeDefaultPaint || includeDiffusedPaint) {
			barCount = 1;
		} else {
			throw new IllegalArgumentException("barCount = 0");
		}
		final int height = heightPerBar*barCount;
		final int totalFrames = 50;
		
		ResettableAnimationReader animation = new ResettableAnimationReader() {
			int frameCtr = 0;

			BufferedImage bi;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(frameCtr==totalFrames) return null;
				
				if(bi==null || cloneImage) {
					bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				}
				
				float f =  ((float)frameCtr)/((float)totalFrames-1f);
				Color c3 = ColorBandDemo.tween(c1, c2, f );
				
				Graphics2D g = bi.createGraphics();
				
				//throw every hint at it in case Java's implementation changes in the future:
				g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				
				int y = 0;
				if(includeDefaultPaint) {
					g.setPaint(new GradientPaint(0, y, c1, width, y, c3));
					g.fillRect(0,0,width,heightPerBar);
					y += heightPerBar;
				}
				if(includeDiffusedPaint) {
					g.setPaint(new GradientTexturePaint(0, y, c1, width, y, c3));
					g.fillRect(0,y,width,heightPerBar);
				}

				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				BlogHelper.drawString(g, (new DecimalFormat("#")).format(f*100)+"%", 3, height-3, false);
				
				g.dispose();
				
				frameCtr++;
				
				return bi;
			}

			public double getDuration() {
				double sum = 0;
				for(int a = 0; a<totalFrames; a++) {
					sum += getFrameDuration(a);
				}
				return sum;
			}

			public int getFrameCount() {
				return totalFrames;
			}

			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}

			public double getFrameDuration() {
				return getFrameDuration(frameCtr-1);
			}

			public double getFrameDuration(int index) {
				if(index==0) {
					return 2;
				} else if(index==totalFrames-1) {
					return 5;
				}
				return .05;
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
