/*
 * @(#)BrushStrokeDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.awt;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.bric.blog.Blurb;
import com.bric.blog.ResourceSample;
import com.bric.swing.StrokeDemo;

/** A simple demo program showing off the {@link com.bric.awt.BrushStroke}.
 * 
 *
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/BrushStrokeDemo/sample.png" alt="new&#160;com.bric.awt.BrushStrokeDemo()">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@Blurb (
filename = "BrushStroke",
title = "Strokes: a Brush Stroke",
releaseDate = "April 2007",
summary = "A stroke based on rendering several smaller strokes in parallel along a path.",
instructions = "This applet showcases a stroke composed of dozens of parallel partial strokes, resembling a brush.\n"+
"<p>Use the two spinners to control the width and thickness of the stroke. There is blue text on the preview "+
"that displays the time it took to render everything.",
link = "http://javagraphics.blogspot.com/2007/04/strokes-brush-stroke.html",
sandboxDemo = true
)
@ResourceSample( sample="new com.bric.awt.BrushStrokeDemo()" )
public class BrushStrokeDemo extends StrokeDemo {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		try {
			BrushStrokeDemo d = new BrushStrokeDemo();
			JFrame f = new JFrame();
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().add(d);
			f.pack();
			f.setVisible(true);
		} catch(NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	public BrushStrokeDemo() throws NoSuchMethodException {
		super(BrushStroke.class, BrushStroke.class.getConstructor(new Class[] {
			Float.TYPE,
			Float.TYPE
		}),
		new JLabel[] {
			new JLabel("Starting Width:"),
			new JLabel("Thickness:")
		},
		new JSpinner[] {
			new JSpinner(new SpinnerNumberModel(1,.05,20,.1)),
			new JSpinner(new SpinnerNumberModel(.5,0,1,.05)),
		});
	}
}