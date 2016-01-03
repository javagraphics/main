/*
 * @(#)Transition3DDemo.java
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
package com.bric.image.transition;

import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.blog.Blurb;

/** A demo of the {@link Transition3D} classes.
 * 
 * @see com.bric.image.transition.Transition2DDemo
 */
@Blurb (
filename = "Transition3D",
title = "Images: 3D Transforms and Transitions",
releaseDate = "April 2014",
summary = "This explores how to render <code>BufferedImages</code> through <code>PerspectiveTransforms</code>, "+
"and offers a few new 3D-based transitions.",
instructions = "This applet demonstrates simple 3D transitions.\n"+
"<p>Use the JComboBox to select your transition, the spinner to specify your duration, and the auto-looping playback will show you what that transition looks like.\n"+
"<p>You can also pause the playback and drag the scrubber manually to see how each transition progresses.",
link = "http://javagraphics.blogspot.com/2014/05/images-3d-transitions-and.html",
sandboxDemo = true
)
public class Transition3DDemo extends TransitionDemo {
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
				
				Transition3DDemo w = new Transition3DDemo();
				JFrame frame = new JFrame("Transition2DDemo");
		        
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(w);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

    static Transition[] transitions = new Transition[] {
			new CubeTransition3D(Transition.LEFT, false),
			new CubeTransition3D(Transition.RIGHT, false),
			new CubeTransition3D(Transition.UP, false),
			new CubeTransition3D(Transition.DOWN, false),
			new CubeTransition3D(Transition.LEFT, true),
			new CubeTransition3D(Transition.RIGHT, true),
			new CubeTransition3D(Transition.UP, true),
			new CubeTransition3D(Transition.DOWN, true),
			new FlipTransition3D(Transition.LEFT, false),
			new FlipTransition3D(Transition.RIGHT, false),
			new FlipTransition3D(Transition.UP, false),
			new FlipTransition3D(Transition.DOWN, false),
			new FlipTransition3D(Transition.LEFT, true),
			new FlipTransition3D(Transition.RIGHT, true),
			new FlipTransition3D(Transition.UP, true),
			new FlipTransition3D(Transition.DOWN, true)
    };
    
    public Transition3DDemo() {
    	super(transitions, true);
    }
    
    @Override
    public RenderingHints getQualityHints() {
    	RenderingHints rh = super.getQualityHints();
    	rh.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    	return rh;
    }
}
