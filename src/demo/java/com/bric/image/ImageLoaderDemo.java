/*
 * @(#)ImageLoaderDemo.java
 *
 * $Date: 2015-03-17 01:05:53 +0100 (Di, 17 Mär 2015) $
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
package com.bric.image;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.swing.JFrame;

import com.bric.blog.Blurb;
import com.bric.util.JVM;

/** A demo app for the {@link ImageLoader} class.
 */ 
@Blurb (
filename = "ImageLoader",
title = "Images: Studying MediaTracker",
releaseDate = "April 2007",
summary = "I never did trust <a href=\"http://download.oracle.com/javase/6/docs/api/java/awt/MediaTracker.html\">MediaTracker</a>. "+
"(Why does it require a <code>java.awt.Component</code> to tell if an image is loaded?)\n"+
"<p>Here I wrote my own class that converts abstract <code>java.awt.Images</code> into <code>BufferedImages</code>.",
link = "http://javagraphics.blogspot.com/2007/04/images-studying-mediatracker.html",
sandboxDemo = false
)
public class ImageLoaderDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame();
			FileDialog fd = new FileDialog(frame);
			fd.setVisible(true);
			if(fd.getFile()==null)
				throw new RuntimeException("User cancelled");
			File file = new File(fd.getDirectory()+fd.getFile());

			JVM.printProfile();
			System.out.println("ImageLoader Tests:");
			System.out.println("File: "+file.getAbsolutePath());

			Image img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
			BufferedImage bi = ImageLoader.createImage(img, file.getAbsolutePath());
			System.out.println("Dimensions: "+bi.getWidth()+"x"+bi.getHeight());
			
			int SIZE = 100; //the number of tests
			
			long[] mem = new long[SIZE];
			long[] time = new long[mem.length];
			long[] mem2 = new long[mem.length];
			long[] time2 = new long[mem.length];
			long[] mem3 = new long[mem.length];
			long[] time3 = new long[mem.length];
			int imageLoaderType = -1;
			int imageIOType = -1;

			for(int a = 0; a<mem2.length; a++) {
				mem2[a] = Runtime.getRuntime().freeMemory();
				time2[a] = System.currentTimeMillis();
				Image i = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
				BufferedImage bi2 = ImageLoader.createImage(i, file.getAbsolutePath());
				time2[a] = System.currentTimeMillis()-time2[a];
				mem2[a] = mem2[a]-Runtime.getRuntime().freeMemory();
				imageLoaderType = bi2.getType();
				i.flush();
				bi2.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem2);
			Arrays.sort(time2);
			
			for(int a = 0; a<mem.length; a++) {
				mem[a] = Runtime.getRuntime().freeMemory();
				time[a] = System.currentTimeMillis();
				Image i = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
				createImageWithMediaTracker(i,frame).flush();
				time[a] = System.currentTimeMillis()-time[a];
				mem[a] = mem[a]-Runtime.getRuntime().freeMemory();
				i.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem);
			Arrays.sort(time);

			for(int a = 0; a<mem.length; a++) {
				mem3[a] = Runtime.getRuntime().freeMemory();
				time3[a] = System.currentTimeMillis();
				BufferedImage bi2 = javax.imageio.ImageIO.read(file);
				time3[a] = System.currentTimeMillis()-time3[a];
				mem3[a] = mem3[a]-Runtime.getRuntime().freeMemory();
				imageIOType = bi2.getType();
				bi2.flush();
				
				System.gc();
				System.runFinalization();
				System.gc();
			}
			Arrays.sort(mem3);
			Arrays.sort(time3);

			System.out.println("\tCreating a BufferedImage:");
			System.out.println("MediaTracker took "+time[SIZE/2]+" ms, "+mem[SIZE/2]+" bytes");
			
			System.out.println("ImageLoader took "+time2[SIZE/2]+" ms, "+mem2[SIZE/2]+" bytes (creating "+getType(imageLoaderType)+")");
			System.out.println("(So ImageLoader took "+(time2[SIZE/2]*100/time[SIZE/2])+"% of the time, and "+(mem2[SIZE/2]*100/mem[SIZE/2])+"% of the memory)");

			System.out.println("ImageIO took "+time3[SIZE/2]+" ms, "+mem3[SIZE/2]+" bytes (creating "+getType(imageIOType)+")");
			System.out.println("(So ImageLoader took "+(time2[SIZE/3]*100/time3[SIZE/2])+"% of the time, and "+(mem2[SIZE/2]*100/mem3[SIZE/2])+"% of the memory)");
			
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/** For the main() tests, this returns the name of the BufferedImage type provided */
	private static String getType(int imageType) {
		Class<?> c = BufferedImage.class;
		try {
			Field[] f = c.getFields();
			for(int a = 0; a<f.length; a++) {
				if((f[a].getModifiers() & Modifier.STATIC) > 0) {
					Object value = f[a].get(null);
					if(value instanceof Number && 
							f[a].getName().startsWith("TYPE_") && 
							((Number)value).intValue()==imageType) {
						return f[a].getName();
					}
				}
			}
		} catch(Throwable t) {}
		return "Unknown";
	}
	
	private static BufferedImage createImageWithMediaTracker(Image i,Component c) {
		MediaTracker tracker = new MediaTracker(c);
		tracker.addImage(i,0);
		try {
			tracker.waitForAll();
		} catch(InterruptedException e) {
			throw new RuntimeException("MediaTracker failed");
		}
		BufferedImage bi = new BufferedImage(i.getWidth(null),i.getHeight(null),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.drawImage(i,0,0,null);
		g.dispose();
		return bi;
	}
}
