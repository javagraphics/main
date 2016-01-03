/*
 * @(#)TransitionPanelDemoHelper.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

/** This creates animations/images to accompany the {@link TransitionPanelDemo}:
 * <p><img src="https://javagraphics.java.net/resources/transitionpanel.gif" style="clear: right; margin-bottom: 1em; margin-left: 1em;border:1px solid gray" alt="Sample Animation of TransitionPanel">
 * 
 * 
 */
public class TransitionPanelDemoHelper extends BlogHelper {

	/** Creates a file named "transitionpanel.gif" in the directory provided.
	 * 
	 * @param robot the Robot to use to create this sample file.
	 * @param directory the master directory to store all resources/subdirectories in.
	 * @return the file titled "transitionpanel.gif"
	 * @throws Exception if an error occurred creating these demo files.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "transitionpanel.gif");
		ResettableAnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static ResettableAnimationReader createAnimation(Robot robot) throws Exception {
		final TransitionPanelDemo tpd = new TransitionPanelDemo(5);
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				((JComponent)frame.getContentPane()).setBorder(new LineBorder(Color.lightGray));
				frame.getContentPane().add(tpd);
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
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(tpd);
		screenCapture.setPlaybackRate(5);
		screenCapture.setTargetedRepaints(false);
		JButton[] pair1 = tpd.pair1;
		JButton[] pair2 = tpd.pair2;
		JButton[] pair3 = tpd.pair3;
		
		Thread.sleep(5000);
		animateMouse(robot, pair1[1], false, false);
		Thread.sleep(500);
		screenCapture.start();
		Thread.sleep(10000);
		animateMouse(robot, pair1[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair1[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair1[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair1[0], true, true);
		Thread.sleep(10000);
		animateMouse(robot, pair1[0], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair1[0], false, true);

		Thread.sleep(10000);
		animateMouse(robot, pair2[0], true, true);
		Thread.sleep(10000);
		animateMouse(robot, pair2[0], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair2[0], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair2[1], true, true);
		Thread.sleep(10000);
		animateMouse(robot, pair2[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair2[1], false, true);
		
		Thread.sleep(10000);
		animateMouse(robot, pair3[1], true, true);
		Thread.sleep(10000);
		animateMouse(robot, pair3[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair3[1], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair3[0], true, true);
		Thread.sleep(10000);
		animateMouse(robot, pair3[0], false, true);
		Thread.sleep(10000);
		animateMouse(robot, pair3[0], false, true);

		Thread.sleep(10000);
		animateMouse(robot, pair1[1], true, false);
		
		ResettableAnimationReader returnValue = screenCapture.stop(true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.setVisible(false);
			}
		});
		
		return returnValue;
	}
}
