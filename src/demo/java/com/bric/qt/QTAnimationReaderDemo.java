/*
 * @(#)QTAnimationReaderDemo.java
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
package com.bric.qt;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.bric.blog.Blurb;

/** A simple demo for the {@link QTAnimationReader} class.
 */
@Blurb (
filename = "QTAnimationReader",
title = "QTJ: From QDGraphics to BufferedImage",
releaseDate = "April 2007",
summary = "This iterates over each frame an a QuickTime movie. "+
"Each frame is returned as a <code>BufferedImage</code>, and you can see " +
"how long each frame is supposed to be.",
link = "http://javagraphics.blogspot.com/2007/04/qtj-from-qdgraphics-to-bufferedimage.htm",
scrapped = "QuickTime for Java is deprecated and unsupported.",
sandboxDemo = false
)
public class QTAnimationReaderDemo {

	/** A panel displaying a BufferedImage */
	static class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		BufferedImage bi;
		public ImagePanel(BufferedImage bi) {
			this.bi = bi;
			setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(bi,0,0,null);
		}
	}
	
	/** This just demonstrates how to set up a QTAnimationReader.
	 * It's really simple/minimal, but it's copyable code.
     * @param args the application's arguments. (This is unused.)
	 */
	public static void main(String[] args) {
		System.out.println("Running QTAnimationReader on "+System.getProperty("java.version")+", "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
		
		JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FileDialog fileDialog = new FileDialog(frame);
		fileDialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir,String name) {
				name = name.toLowerCase();
				if(name.endsWith(".mov") || name.endsWith(".mpeg") || name.endsWith(".mpg") || name.endsWith(".swf")) {
					return true;
				}
				return false;
			}
		});
		fileDialog.show();
		File file = new File(fileDialog.getDirectory()+fileDialog.getFile());
		try {
			QTAnimationReader r = new QTAnimationReader(file);
			int total = r.getFrameCount();
			ImagePanel p = null;
			for(int f = 0; f<total; f++) {
				BufferedImage bi = r.getNextFrame(false);
				
				//replace this code with whatever you want to do
				//to process bi:
				if(f==0) {
					p = new ImagePanel(bi);
					frame.getContentPane().add(p);
					frame.pack();
					frame.show();
				}
				p.repaint();
				
				//just to make sure you can see it
				try {
					Thread.sleep(80);
				} catch(InterruptedException e) { 
					Thread.yield();
				}
				//end of code block to replace
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished");
	}
}
