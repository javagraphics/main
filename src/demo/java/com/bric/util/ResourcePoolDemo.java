/*
 * @(#)ResourcePoolDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;

/** This simple app makes a few comparison related to
 * whether the ResourcePool marks an improvement or not.
 * 
 **/
@Blurb (
filename = "ResourcePoolDemo",
title = "Performance: Constructing Arrays and Images",
releaseDate = "April 2014",
summary = "This examines the cost of constructing arrays and BufferedImages, vs caching and reusing existing arrays.\n"+
"<p>This is intended to be used for highly repetitive tasks (such as batch image processing or rendering work).",
instructions = "Just wait a few seconds for the applet to begin. A table of data will be printed to the applet below, ending with \"done\" when finished.",
link = "http://javagraphics.blogspot.com/2014/04/performance-constructing-arrays-and.html",
sandboxDemo = true
)
public class ResourcePoolDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JFrame f = new JFrame();
				f.getContentPane().add(new ResourcePoolDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	JTextArea textArea = new JTextArea();
	
	public ResourcePoolDemo() {
		textArea.setEditable(false);
		getContentPane().add(new JScrollPane(textArea));
		Thread thread = new Thread() {
			public void run() {
				textArea.append(JVM.getProfile()+"\n");
				try {
					//give everyone else a chance to catch up:
					sleep(5000);
					textArea.append("Image Comparison:\n");
					textArea.append("Paint\tClear\tConstruction (ms)\tResourcePool (ms)\n");
					textArea.append( "true\ttrue\t"+runImageTest(false, true, true)+"\t"+runImageTest(true, true, true)+"\n" );
					textArea.append( "true\tfalse\t"+runImageTest(false, true, false)+"\t"+runImageTest(true, true, false)+"\n" );
					textArea.append( "false\ttrue\t"+runImageTest(false, false, true)+"\t"+runImageTest(true, false, true)+"\n" );
					textArea.append( "false\tfalse\t"+runImageTest(false, false, false)+"\t"+runImageTest(true, false, false)+"\n" );
					textArea.append("\nPrimitive\tConstruction (ms)\tResourcePool (ms)\n");
					textArea.append("int[]\t"+runIntTest(false)+"\t"+runIntTest(true)+"\n");
					textArea.append("float[]\t"+runFloatTest(false)+"\t"+runFloatTest(true)+"\n");
					textArea.append("long[]\t"+runLongTest(false)+"\t"+runLongTest(true)+"\n");
					textArea.append("double[]\t"+runDoubleTest(false)+"\t"+runDoubleTest(true)+"\n");
					runScalingLongTest();
					textArea.append("done"+"\n");
				} catch(Throwable t) {
					t.printStackTrace();
				}
			}
		};
		thread.start();
		getContentPane().setPreferredSize(new Dimension(400, 400));
	}
	
	protected String runImageTest(boolean useCache,boolean paint,boolean clearImage) {
		long[] times = new long[20];
		for(int a = 0; a<times.length; a++) {
			long start = System.currentTimeMillis();
			for(int b = 0; b<50; b++) {
				BufferedImage bi;
				if(useCache) {
					bi = ResourcePool.get().getImage(1000, 1000, BufferedImage.TYPE_INT_ARGB, clearImage);
				} else {
					bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
				}
				if(paint) {
					Graphics2D g = bi.createGraphics();
					g.setPaint(new Color(255,0,0,100));
					g.fillRect(0,0,1000,1000);
					g.dispose();
				}
				if(useCache) {
					ResourcePool.get().put(bi);
				}
			}
			times[a] = System.currentTimeMillis()-start;
		}
		Arrays.sort(times);
		return times[times.length/2]+"";
	}
	
	protected String runIntTest(boolean useCache) {
		long[] times = new long[20];
		int arraySize = 1000000;
		for(int a = 0; a<times.length; a++) {
			long start = System.currentTimeMillis();
			for(int b = 0; b<50; b++) {
				int[] array;
				if(useCache) {
					array = ResourcePool.get().getIntArray(arraySize);
				} else {
					array = new int[arraySize];
				}
				//do something--anything--to make 100% sure
				//compilers don't discard this unused array
				array[arraySize/2] = 1;
				if(useCache) {
					ResourcePool.get().put(array);
				}
			}
			times[a] = System.currentTimeMillis()-start;
		}
		Arrays.sort(times);
		return times[times.length/2]+"";
	}
	
	protected String runFloatTest(boolean useCache) {
		long[] times = new long[20];
		int arraySize = 1000000;
		for(int a = 0; a<times.length; a++) {
			long start = System.currentTimeMillis();
			for(int b = 0; b<50; b++) {
				float[] array;
				if(useCache) {
					array = ResourcePool.get().getFloatArray(arraySize);
				} else {
					array = new float[arraySize];
				}
				array[arraySize/2] = 1;
				if(useCache) {
					ResourcePool.get().put(array);
				}
			}
			times[a] = System.currentTimeMillis()-start;
		}
		Arrays.sort(times);
		return times[times.length/2]+"";
	}
	
	protected String runLongTest(boolean useCache) {
		long[] times = new long[20];
		int arraySize = 1000000;
		for(int a = 0; a<times.length; a++) {
			long start = System.currentTimeMillis();
			for(int b = 0; b<50; b++) {
				long[] array;
				if(useCache) {
					array = ResourcePool.get().getLongArray(arraySize);
				} else {
					array = new long[arraySize];
				}
				array[arraySize/2] = 1;
				if(useCache) {
					ResourcePool.get().put(array);
				}
			}
			times[a] = System.currentTimeMillis()-start;
		}
		Arrays.sort(times);
		return times[times.length/2]+"";
	}
	
	protected void runScalingLongTest() {
		long[] times = new long[20];
		textArea.append("\nScaling Test:\n");
		textArea.append("Array Size\tResourcePool\tConstruction\tPercent\n");
		for(int arraySize = 10; arraySize<=1000000; arraySize*=10) {
			StringBuffer sb = new StringBuffer(""+arraySize);
			int loopCount = 50 * 1000000 / arraySize;
			for(int a = 0; a<times.length; a++) {
				long start = System.currentTimeMillis();
				for(int b = 0; b<loopCount; b++) {
					long[] array = ResourcePool.get().getLongArray(arraySize);
					array[arraySize/2] = 1;
					ResourcePool.get().put(array);
				}
				times[a] = System.currentTimeMillis()-start;
			}
			Arrays.sort(times);
			double poolTime = times[times.length/2];
			sb.append("\t"+poolTime);
			for(int a = 0; a<times.length; a++) {
				long start = System.currentTimeMillis();
				for(int b = 0; b<loopCount; b++) {
					long[] array = new long[arraySize];
					array[arraySize/2] = 1;
				}
				times[a] = System.currentTimeMillis()-start;
			}
			Arrays.sort(times);
			double constructionTime = times[times.length/2];
			sb.append("\t"+constructionTime);
			double fraction = poolTime / constructionTime * 100;
			sb.append("\t"+(new DecimalFormat("#.##")).format(fraction)+"%\n");
			textArea.append(sb.toString());
		}
	}
	
	protected String runDoubleTest(boolean useCache) {
		long[] times = new long[20];
		int arraySize = 1000000;
		for(int a = 0; a<times.length; a++) {
			long start = System.currentTimeMillis();
			for(int b = 0; b<50; b++) {
				double[] array;
				if(useCache) {
					array = ResourcePool.get().getDoubleArray(arraySize);
				} else {
					array = new double[arraySize];
				}
				array[arraySize/2] = 1;
				if(useCache) {
					ResourcePool.get().put(array);
				}
			}
			times[a] = System.currentTimeMillis()-start;
		}
		Arrays.sort(times);
		return times[times.length/2]+"";
	}
}
