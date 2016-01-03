/*
 * @(#)TransformedTexturePaintDemoHelper.java
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
package com.bric.awt;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.animation.AnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.RobotScreenCapture;
import com.bric.qt.io.JPEGMovWriter;

public class TransformedTexturePaintDemoHelper extends BlogHelper {

	/** Creates a file named "transformedTexturePaint.mov" in the directory provided.
	 * 
	 * @param robot the robot to use to create the movie.
	 * @param directory the directory to store the movie file in.
	 * @return the file "transformedTexturePaint.mov"
	 * 
	 * @throws Exception if an error occurs creating the animation
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File movFile = new File(directory, "transformedTexturePaint.mov");

		AnimationReader reader = createAnimation(robot);
		JPEGMovWriter w = new JPEGMovWriter(movFile);
		w.addFrames(reader, null);
		w.close(true);
		return movFile;
	}
	
	private static AnimationReader createAnimation(Robot robot) throws Exception {
		final TransformedTexturePaintDemo ttpd = new TransformedTexturePaintDemo();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.getContentPane().add(ttpd);
				frame.pack();
				Dimension d = frame.getPreferredSize();
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
		
		RobotScreenCapture screenCapture = new RobotScreenCapture(bounds);
		screenCapture.setTargetedRepaints(false);
		screenCapture.setPlaybackRate(2);

		Thread.sleep(1000);
		

		Point offset = new Point(0,0);
		SwingUtilities.convertPointToScreen(offset, ttpd);
		Point2D p = ttpd.panel.p1;
		robot.mouseMove((int)p.getX()+offset.x, (int)p.getY()+offset.y);
		screenCapture.start();
		Thread.sleep(1000);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		for(int k = 0; k<50; k++) {
			robot.mouseMove((int)p.getX()+offset.x-k, (int)p.getY()+offset.y-k);
			Thread.sleep(200);
		}
		robot.mouseRelease(InputEvent.BUTTON1_MASK);

		Thread.sleep(1000);
		
		p = ttpd.panel.p2;
		robot.mouseMove((int)p.getX()+offset.x, (int)p.getY()+offset.y );
		robot.mousePress(InputEvent.BUTTON1_MASK);
		for(int k = 0; k<50; k++) {
			robot.mouseMove((int)p.getX()+offset.x+k, (int)(p.getY()+offset.y+.5*k) );
			ttpd.panel.updatePaint();
			Thread.sleep(200);
		}
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		
		Thread.sleep(1000);
		
		AnimationReader returnValue = screenCapture.stop(true);
		
		frame.setVisible(false);
		
		return returnValue;
	}
}
