/*
 * @(#)CollapsibleContainerDemoHelper.java
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

/** This creates animations/images to accompany the {@link CollapsibleContainerDemo}:
  * <p><img src="https://javagraphics.java.net/resources/collapsiblecontainer.gif" style="border:1px solid gray" alt="CollapsibleContainer Animation">
  *  
  */
public class CollapsibleContainerDemoHelper extends BlogHelper {

	/** Creates a file named "collapsiblecontainer.gif" in the directory provided.
	 * 
	 * @param robot the Robot to use to create this sample file.
	 * @param directory the master directory to store all resources/subdirectories in.
	 * @return the file titled "collapsiblecontainer.gif"
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "collapsiblecontainer.gif");
		ResettableAnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static ResettableAnimationReader createAnimation(Robot robot) throws Exception {
		final CollapsibleContainerDemo ccd = new CollapsibleContainerDemo(5);
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				((JComponent)frame.getContentPane()).setBorder(new LineBorder(Color.lightGray));
				frame.getContentPane().add(ccd);
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
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(ccd);
		screenCapture.setPlaybackRate(5);
		screenCapture.setTargetedRepaints(false);
		JButton button2 = ccd.container.getHeader(ccd.section2);
		JButton button3 = ccd.container.getHeader(ccd.section3);
		
		
		animateMouse(robot, button2, false, false);
		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(10000);
		animateMouse(robot, button2, false, true);
		Thread.sleep(20000);
		animateMouse(robot, button3, true, true);
		Thread.sleep(20000);
		animateMouse(robot, button2, true, true);
		Thread.sleep(20000);
		animateMouse(robot, button3, true, true);
		Thread.sleep(20000);
		animateMouse(robot, button2, true, false);
		Thread.sleep(10000);
		ResettableAnimationReader returnValue = screenCapture.stop(true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.setVisible(false);
			}
		});
	
		return returnValue;
	}
}
