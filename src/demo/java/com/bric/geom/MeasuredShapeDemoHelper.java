/*
 * @(#)MeasuredShapeDemoHelper.java
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
package com.bric.geom;

import java.awt.Color;
import java.awt.Robot;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.capture.ComponentPaintCapture;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class MeasuredShapeDemoHelper extends BlogHelper {

	/** Creates a file named "measured-shape.gif" in the directory provided.
	 * 
	 * @param robot the Robot to use to create this sample file.
	 * @param directory the master directory to store all resources/subdirectories in.
	 * @return the file titled "measured-shape.gif"
	 * @throws Exception if an error occurred creating this demo file.
	 */
	public static File createAnimation(Robot robot,File directory) throws Exception {
		File gifFile = new File(directory, "measured-shape.gif");
		ResettableAnimationReader reader = createAnimation(robot);
		GifWriter.write(gifFile, reader, ColorReduction.FROM_FIRST_FRAME);
		return gifFile;
	}
	
	private static ResettableAnimationReader createAnimation(Robot robot) throws Exception {
		final MeasuredShapeDemo md = new MeasuredShapeDemo();
		final JFrame frame = new JFrame();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				((JComponent)frame.getContentPane()).setBorder(new LineBorder(Color.lightGray));
				frame.getContentPane().add(md);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
		
		ComponentPaintCapture screenCapture = new ComponentPaintCapture(md.getContentPane());
		screenCapture.setPlaybackRate(3);
		screenCapture.setTargetedRepaints(false);
		
		Thread.sleep(1000);
		screenCapture.start();
		Thread.sleep(1000);
		
		md.percentSlider.requestFocus();
		for(int v = 100; v>=50f; v-=5) {
			md.percentSlider.setValue(v);
			beat(300);
		}
		
		Thread.sleep(1000);
		md.offsetSlider.requestFocus();
		for(int v = 0; v<=100; v+=5) {
			md.offsetSlider.setValue(v);
			beat(300);
		}

		Thread.sleep(1000);
		md.percentSlider.requestFocus();
		for(int v = 50; v<=100; v+=5) {
			md.percentSlider.setValue(v);
			beat(300);
		}
		Thread.sleep(1000);
		
		ResettableAnimationReader returnValue = screenCapture.stop(true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.setVisible(false);
			}
		});
		
		return returnValue;
	}
}
