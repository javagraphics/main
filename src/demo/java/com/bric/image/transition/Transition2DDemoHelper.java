/*
 * @(#)Transition2DDemoHelper.java
 *
 * $Date: 2014-05-07 01:27:53 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.image.transition;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class Transition2DDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		BufferedImage frameA = AbstractTransition.createImage(400,"A",true,true);
		BufferedImage frameB = AbstractTransition.createImage(400,"B",false,true);
		BufferedImage finalImage = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
		Transition2D transition = new SwivelTransition2D(Color.white,Transition.COUNTER_CLOCKWISE);
		float fraction = .25f;
		Graphics2D g = finalImage.createGraphics();
		g.setRenderingHints(Transition2DDemo.createQualityHints());
		transition.paint(g, frameA, frameB, fraction);
		g.setPaint(new GradientPaint(0,400,new Color(255,255,255,0),0,500,Color.white));
		g.fillRect(0, 400, finalImage.getWidth(), 100);
		g.dispose();
		return finalImage;
	}
	
	public static File createSamples(File dir) throws ClassNotFoundException, IOException {
		dir = new File(dir, "transition");
		if(!dir.mkdirs())
			throw new RuntimeException("mkdirs failed for "+dir.getAbsolutePath());
		
		Class<?>[] transitionClasses = getNonAbstractSubclasses(Transition.class, "com.bric.image.transition");
		for(Class<?> transitionClass : transitionClasses) {
			Transition[] transitions = new Transition[] {};
			try {
				Method m = transitionClass.getMethod("getDemoTransitions", new Class[] {});
				transitions = (Transition[])m.invoke(null, new Object[] {});
			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			} catch (IllegalAccessException e) {
			} catch (IllegalArgumentException e) {
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			if(transitions.length==0) {
				try {
					transitions = new Transition[] { (Transition)transitionClass.newInstance() };
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}
			
			String dirName = transitionClass.getName();
			dirName = dirName.substring(dirName.lastIndexOf('.')+1);
			File tDir = new File(dir, dirName);
			if(!tDir.mkdirs())
				throw new RuntimeException("mkdirs failed for "+tDir.getAbsolutePath());
			
			for(Transition transition : transitions) {
				String name = transition.toString().replace(" ", "");
				File gifFile = new File(tDir, name+".gif");
				writeTransition(transition, gifFile);
				System.out.println("wrote "+gifFile.getAbsolutePath());
			}
		}
		
		return dir;
	}
	
	private static void writeTransition(final Transition transition,File gifFile) throws IOException {
		final int size = 100;
		final BufferedImage bi1 = AbstractTransition.createImage(size, "A", true, false);
		final BufferedImage bi2 = AbstractTransition.createImage(size, "B", false, false);
		final int totalFrames = 200;
		final int middleFrame = totalFrames/2;
		
		ResettableAnimationReader animation = new ResettableAnimationReader() {
			int ctr = 0;

			BufferedImage bi;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(ctr==totalFrames) return null;
				
				if(bi==null || cloneImage) {
					bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
				}
				Graphics2D g = bi.createGraphics();
				g.setColor(Color.black);
				g.fillRect(0,0,size,size);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				if(transition instanceof Transition3D) {
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				} else {
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				}
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				float f;
				if(ctr<middleFrame) {
					f = ((float)ctr)/((float)middleFrame);
					transition.paint(g, bi1, bi2, f);
				} else {
					f = ((float)(ctr-middleFrame))/((float)middleFrame);
					transition.paint(g, bi2, bi1, f);
				}
				g.dispose();
				
				ctr++;
				
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
				return getFrameDuration(ctr-1);
			}

			public double getFrameDuration(int index) {
				if(index==0 || index==middleFrame) {
					return 2;
				}
				return .007;
			}

			public int getWidth() {
				return size;
			}

			public int getHeight() {
				return size;
			}

			public void reset() {
				ctr = 0;
			}
			
		};
		GifWriter.write(gifFile, animation, ColorReduction.FROM_ALL_FRAMES);
	}
}
