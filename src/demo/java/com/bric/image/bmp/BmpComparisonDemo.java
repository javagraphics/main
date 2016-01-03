/*
 * @(#)BmpComparisonDemo.java
 *
 * $Date$
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
package com.bric.image.bmp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;
import com.bric.image.thumbnail.Thumbnail;
import com.bric.swing.BasicConsole;
import com.bric.swing.jnlp.FilePanel;
import com.bric.swing.jnlp.FilePanel.FileData;
import com.bric.util.JVM;

/** This application prompts you for a BMP file and then executes a few tests.
 * <P>The first test measures the time and memory used to create a thumbnail, and
 * the second test measures the time and memory used to write a file.
 * <p>The console is printed to the screen using a BasicConsole to see the results.
 * <p>Also the BMP image itself is displayed in a separate window so you can
 * visually confirm that the results are what you expect.
 */
@Blurb (
filename = "BmpComparisonDemo",
title = "Images: BMPs and Thumbnails",
releaseDate = "June 2010",
summary = "This provides an efficient mechanism for creating BMP thumbnails, "+
"and a crude BMP decoder/encoder.",
link = "http://javagraphics.blogspot.com/2010/06/images-bmps-and-thumbnails.html",
sandboxDemo = true
)
public class BmpComparisonDemo extends JPanel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("BmpComparisonDemo");
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(new BmpComparisonDemo());
				f.pack();
				f.setVisible(true);
			}
		});
	}


	FilePanel filePanel = new FilePanel("File:", new String[] {"bmp"});
	BasicConsole console = new BasicConsole(false, true);
	PrintStream out = console.createPrintStream(false);
	PrintStream err = console.createPrintStream(true);
	PrintStream blue = console.createPrintStream(new Color(0x550000ff,true));
	
	
	public BmpComparisonDemo() {
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(3,3,3,3);
		c.weightx = 1; c.weighty = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.BOTH;
		add(filePanel,c);
		c.gridy++;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.SOUTH;
		add(new JLabel("Console:"),c);
		c.gridy = 4; c.gridx = 0; c.gridwidth = GridBagConstraints.REMAINDER;
		
		c.gridy++; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
		JScrollPane scrollPane = new JScrollPane(console);
		scrollPane.setPreferredSize(new Dimension(600,600));
		add(scrollPane,c);
		
		filePanel.addPropertyChangeListener(FilePanel.FILE_DATA_KEY, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					final FileData bmpFile = filePanel.getFileData();
					Thread testThread = new Thread("BmpComparisonDemo thread") {
						public void run() {
							try {
								//verify we read it correctly:
								BufferedImage bi = show(bmpFile);
								runThumbnailTest(bmpFile, out, blue);
								runEncodeTest(bi, out, blue);
							} catch(Throwable e) {
								e.printStackTrace(err);
							} finally {
								out.println("\nFinished.");
							}
						}
					};
					testThread.start();
				} catch(Exception e) {
					e.printStackTrace(err);
				}
			}
		});

		out.println(JVM.getProfile());
		
		out.println("\nAfter selecting a file, this compares the new bric.bmp.* classes"
				+ "\n(highlighted in blue) with existing ImageIO classes.\n");
	}
	
	private static BufferedImage getImageIOThumbnail(InputStream file,Dimension maxSize) throws IOException {
		try(ImageInputStream in = ImageIO.createImageInputStream(file)) {
			Iterator<ImageReader> i = ImageIO.getImageReaders(in);
			while(i.hasNext()) {
				ImageReader r = i.next();
				r.setInput(in,false);
				//ImageReadParam p = new ImageReadParam();
				//p.setSourceRenderSize(maxSize);
				BufferedImage bigImage = r.read(0);
				return Thumbnail.Plain.create(bigImage, maxSize);
			}
			throw new IOException("Unsupported file");
		}
	}

	private static BufferedImage show(FileData file) throws IOException {
		try(InputStream in = file.createInputStream()) {
			BufferedImage image = BmpDecoder.readImage(in);
			//this isn't really needed now; it's just a distraction from the demo app:
			/*JFrame f = new JFrame("BMP Verification");
			f.getContentPane().add(new JScrollPane(new JLabel(new ImageIcon(image))));
			f.pack();
			f.setVisible(true);*/
			return image;
		}
	}
	
	private static void runThumbnailTest(FileData file,PrintStream out,PrintStream blue) throws IOException {
		
		out.println("\nMeasuring performance when creating thumbnails:\n");
		out.print("\t");
		blue.print("com.bric");
		out.println("\tImageIO\t");
		out.print("Time (ms)\t");
		
		int repeat = 30;
		long[] times1 = new long[12];
		long[] times2 = new long[times1.length];
		long[] memory1 = new long[times1.length];
		long[] memory2 = new long[times1.length];
		Dimension maxSize = new Dimension(128,128);
		for (int a = 0; a < times1.length; a++) {
			System.runFinalization();
			System.gc();
			System.runFinalization();
			System.gc();
			times1[a] = System.currentTimeMillis();
			memory1[a] = Runtime.getRuntime().freeMemory();
			for(int b = 0; b<repeat; b++) {
				try(InputStream in = file.createInputStream()) {
					@SuppressWarnings("unused")
					BufferedImage bi = BmpDecoder.createThumbnail(in, maxSize);
				}
			}
			times1[a] = System.currentTimeMillis() - times1[a];
			memory1[a] = memory1[a] - Runtime.getRuntime().freeMemory();
		}
		Arrays.sort(times1);
		Arrays.sort(memory1);
		blue.print(times1[times1.length/2]);
		
		for (int a = 0; a < times2.length; a++) {
			System.runFinalization();
			System.gc();
			System.runFinalization();
			System.gc();
			times2[a] = System.currentTimeMillis();
			memory2[a] = Runtime.getRuntime().freeMemory();
			for(int b = 0; b<repeat; b++) {
				try(InputStream in = file.createInputStream()) {
					@SuppressWarnings("unused")
					BufferedImage bi = getImageIOThumbnail(in, maxSize);
				}
			}
			times2[a] = System.currentTimeMillis() - times2[a];
			memory2[a] = memory2[a] - Runtime.getRuntime().freeMemory();
		}
		Arrays.sort(times2);
		Arrays.sort(memory2);
		out.println("\t"+times2[times2.length/2]+"\t");
		
		out.print("Memory (KB)\t");
		blue.print( (memory1[memory1.length/2]/1024));
		out.print( "\t"+(memory2[memory2.length/2]/1024)+"\t");
		out.println();
	}


	private static void runEncodeTest(BufferedImage bi,PrintStream out,PrintStream blue) throws IOException {

		out.println("\nMeasuring performance when encoding BMPs:\n");
		out.print("\t");
		blue.print("com.bric");
		out.println("\tImageIO\t");
		out.print("Time (ms)\t");
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		int repeat = 30;
		long[] times1 = new long[30];
		long[] times2 = new long[times1.length];
		long[] memory1 = new long[times1.length];
		long[] memory2 = new long[times1.length];
		for (int a = 0; a < times1.length; a++) {
			System.runFinalization();
			System.gc();
			System.runFinalization();
			System.gc();
			times1[a] = System.currentTimeMillis();
			memory1[a] = Runtime.getRuntime().freeMemory();
			for(int b = 0; b<repeat; b++) {
				bOut.reset();
				BmpEncoder.write(bi, bOut);
			}
			times1[a] = System.currentTimeMillis() - times1[a];
			memory1[a] = memory1[a] - Runtime.getRuntime().freeMemory();
		}
		Arrays.sort(times1);
		Arrays.sort(memory1);
		blue.print(times1[times1.length/2]);
		
		for (int a = 0; a < times2.length; a++) {
			System.runFinalization();
			System.gc();
			System.runFinalization();
			System.gc();
			times2[a] = System.currentTimeMillis();
			memory2[a] = Runtime.getRuntime().freeMemory();
			for(int b = 0; b<repeat; b++) {
				bOut.reset();
				javax.imageio.ImageIO.write(bi, "bmp", bOut);
			}
			times2[a] = System.currentTimeMillis() - times2[a];
			memory2[a] = memory2[a] - Runtime.getRuntime().freeMemory();
		}
		Arrays.sort(times2);
		Arrays.sort(memory2);
		out.print("\t"+times2[times2.length/2]+"\t");
		out.println();
		out.print("Memory (KB)\t");
		blue.print( (memory1[memory1.length/2]/1024));
		out.print( "\t"+(memory2[memory2.length/2]/1024)+"\t");
		out.println();
	}
}
