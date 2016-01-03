/*
 * @(#)Playback.java
 *
 * $Date: 2014-05-06 21:35:10 +0200 (Di, 06 Mai 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;
import quicktime.std.StdQTConstants;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

/** A simple app demoing how to use QTJ to capture video
 * from a built-in webcam to a BufferedImage on screen.
 * 
 * @deprecated QuickTime for Java is deprecated.
 */
@Deprecated
public class Playback extends JPanel {
	private static final long serialVersionUID = 1L;
	
	QTJImage qtImage;
	SequenceGrabber mGrabber;
	BufferedImage bufferedImage;
	SGVideoChannel mVideo;
	QDGraphics gworld;
	
	boolean playthrough = true;
	long msRepaintDelay = 100;
	long grabbingTime = -1;
	Thread grabberThread = new Thread() {
		@Override
		public void run() {
			while(true) {
				long t = System.currentTimeMillis();
				
				synchronized(Playback.this) {
					if(mGrabber==null)
						return;
					
					try {
						if(mGrabber.isPreviewMode() && mGrabber.idleMore()) {
							mGrabber.idle();
						}
					} catch(QTException e) {
						e.printStackTrace();
						return;
					}
					bufferedImage = qtImage.getImage(bufferedImage);
					process(bufferedImage);
				}
				postProcess(bufferedImage);
				
				long t2 = System.currentTimeMillis();
				grabbingTime = t2-t;
				
				while(t2-t<msRepaintDelay) {
					t2 = System.currentTimeMillis();
					long d = msRepaintDelay-(t2-t);
					if(d<0) d = 1;
					try {
						Thread.sleep(d);
					} catch(InterruptedException e) {
						Thread.yield();
					}
				}
				if(playthrough)
					repaint();
			}
		}
	};
	
	public Playback(Dimension d) throws QTException {
		QDRect qdBounds = new QDRect(0,0,d.width,d.height);
		gworld = new QDGraphics( QDConstants.k32ARGBPixelFormat, qdBounds );
		qtImage = new QTJImage(gworld);
		mGrabber = new SequenceGrabber();
		mGrabber.setGWorld(gworld,null);
		mVideo = new SGVideoChannel(mGrabber);
		mVideo.setBounds(qdBounds);
		mVideo.setUsage( StdQTConstants.seqGrabPreview );
		mGrabber.prepare( true, false);
		mGrabber.startPreview();
		setPreferredSize(d);
		
		grabberThread.start();
	}
	
	/** An optional method to filter or decorate the incoming image.
	 * By default this does nothing, but subclasses can override it.
	 * This is invoked while this Playback object is synchronized.
	 */
	protected void process(BufferedImage bi) {}

	/** An optional method to use the captured image.
	 * By default this does nothing, but subclasses can override it.
	 * This is NOT invoked while this Playback object is synchronized.
	 */
	protected void postProcess(BufferedImage bi) {}
	
	Font font = new Font("Default",0,13);
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(playthrough) {
			synchronized(Playback.this) {
				if(bufferedImage!=null)
					g.drawImage(bufferedImage,0,0,null);
			}
			String s = "Frame update took: "+grabbingTime+" ms";
			drawString(g, s);
		} else {
			drawString(g, "Playthrough not active");
		}
	}
	
	private void drawString(Graphics g, String s) {
		g.setColor(Color.black);
		g.setFont(font);
		g.drawString(s, 3, 20);
		g.setColor(Color.white);
		g.drawString(s, 3, 19);
	}
	
	@Override
	protected void finalize() throws Throwable {
		System.out.println("end reached");
		synchronized(Playback.this) {
			if(mGrabber.isPreviewMode()) 
				mGrabber.stop();
			mGrabber.disposeChannel(mVideo);
			mGrabber.release();
			mGrabber.disposeQTObject();
			mGrabber = null;
			mVideo.disposeQTObject();
			mVideo = null;
			gworld.disposeQTObject();
			gworld = null;
			
			qtImage = null;
		}
		
		QTSession.close();
	}
}
