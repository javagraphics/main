/*
 * @(#)Transition2DDemo.java
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

import javax.swing.JFrame;

import com.bric.blog.Blurb;

/** A demo of the {@link Transition2D} architecture.
 * 
 * @see com.bric.image.transition.Transition3DDemo
 *
 */
@Blurb (
filename = "Transition2D",
title = "Slideshows: Transitions & SWFs",
releaseDate = "April 2007",
summary = "A fun set of transitions defined with a simple vector-graphics-ish "+
"<a href=\"https://javagraphics.java.net/doc/com/bric/image/transition/Transition2D.html\">architecture</a>.\n"+
"<p>The demo includes a player and minimal Flash export.",
instructions = "This applet demonstrates animations using the <code>Transition2D</code> architecture.\n"+
"<p>Use the JComboBox to select your transition, the spinner to specify your duration, and the auto-looping playback will show you what that transition looks like.\n"+
"<p>You can also pause the playback and drag the scrubber manually to see how each transition progresses.",
link = "http://javagraphics.blogspot.com/2007/04/slideshows-transitions-swf.html",
sandboxDemo = true
)
public class Transition2DDemo extends TransitionDemo {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		Transition2DDemo w = new Transition2DDemo();
		JFrame frame = new JFrame("Transition2DDemo");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(w);
		frame.pack();
		frame.setVisible(true);
	}


    static Transition[] transitions = new Transition[] {
			new BarsTransition2D(Transition2D.HORIZONTAL,true),
        	new BarsTransition2D(Transition2D.HORIZONTAL,false),
        	new BarsTransition2D(Transition2D.VERTICAL,true),
        	new BarsTransition2D(Transition2D.VERTICAL,false),
    		new BlendTransition2D(),
    		new BlindsTransition2D(Transition2D.LEFT),
    		new BlindsTransition2D(Transition2D.RIGHT),
    		new BlindsTransition2D(Transition2D.UP),
    		new BlindsTransition2D(Transition2D.DOWN),
        	new BoxTransition2D(Transition2D.IN),
        	new BoxTransition2D(Transition2D.OUT),
    		new CheckerboardTransition2D(Transition2D.LEFT),
    		new CheckerboardTransition2D(Transition2D.RIGHT),
    		new CheckerboardTransition2D(Transition2D.UP),
    		new CheckerboardTransition2D(Transition2D.DOWN),
        	new CircleTransition2D(Transition2D.IN),
        	new CircleTransition2D(Transition2D.OUT),
			new CollapseTransition2D(),
    		new CurtainTransition2D(),
    		new DiamondsTransition2D(55),
    		new DiamondsTransition2D(90),
    		new DiamondsTransition2D(120),
    		new DocumentaryTransition2D(Transition2D.LEFT),
    		new DocumentaryTransition2D(Transition2D.RIGHT),
    		new DocumentaryTransition2D(Transition2D.UP),
    		new DocumentaryTransition2D(Transition2D.DOWN),
    		new DotsTransition2D(),
			new DropTransition2D(),
			new FlurryTransition2D(Transition2D.IN),
	        new FlurryTransition2D(Transition2D.OUT),
	        new FunkyWipeTransition2D(true),
	        new FunkyWipeTransition2D(false),
			new GooTransition2D(),
			new HalftoneTransition2D(Transition2D.IN),
	        new HalftoneTransition2D(Transition2D.OUT),
	        //new KaleidoscopeTransition2D(),
			new LevitateTransition2D(),
			new MeshShuffleTransition2D(),
	        //new MicroscopeTransition2D(),
	        //new MirageTransition2D(),
    		new MotionBlendTransition2D(),
	        //new RefractiveTransition2D(),
    		new PivotTransition2D(Transition2D.TOP_LEFT, true),
    		new PivotTransition2D(Transition2D.TOP_RIGHT, true),
    		new PivotTransition2D(Transition2D.BOTTOM_LEFT, true),
    		new PivotTransition2D(Transition2D.BOTTOM_RIGHT, true),
    		new PivotTransition2D(Transition2D.TOP_LEFT, false),
    		new PivotTransition2D(Transition2D.TOP_RIGHT, false),
    		new PivotTransition2D(Transition2D.BOTTOM_LEFT, false),
    		new PivotTransition2D(Transition2D.BOTTOM_RIGHT, false),
        	new PushTransition2D(Transition2D.LEFT),
        	new PushTransition2D(Transition2D.RIGHT),
        	new PushTransition2D(Transition2D.UP),
        	new PushTransition2D(Transition2D.DOWN),
    		new RadialWipeTransition2D(Transition2D.CLOCKWISE),
    		new RadialWipeTransition2D(Transition2D.COUNTER_CLOCKWISE),
    		new RevealTransition2D(Transition2D.LEFT),
    		new RevealTransition2D(Transition2D.RIGHT),
    		new RevealTransition2D(Transition2D.UP),
    		new RevealTransition2D(Transition2D.DOWN),
    		new RotateTransition2D(Transition2D.IN),
    		new RotateTransition2D(Transition2D.OUT),
    		new ScaleTransition2D(Transition2D.IN),
    		new ScaleTransition2D(Transition2D.OUT),
			new ScribbleTransition2D(false),
	        new ScribbleTransition2D(true),
        	new SlideTransition2D(Transition2D.LEFT),
        	new SlideTransition2D(Transition2D.RIGHT),
        	new SlideTransition2D(Transition2D.UP),
        	new SlideTransition2D(Transition2D.DOWN),
			new SpiralTransition2D(false),
	        new SpiralTransition2D(true),
    		new SplitTransition2D(Transition2D.HORIZONTAL,false),
    		new SplitTransition2D(Transition2D.VERTICAL,false),
    		new SplitTransition2D(Transition2D.HORIZONTAL,true),
    		new SplitTransition2D(Transition2D.VERTICAL,true),
			new SquareRainTransition2D(),
			new SquaresTransition2D(),
	    	new StarTransition2D(Transition2D.IN),
	    	new StarTransition2D(Transition2D.OUT),
			new StarsTransition2D(Transition2D.LEFT),
	        new StarsTransition2D(Transition2D.RIGHT),
			new SwivelTransition2D(Transition2D.CLOCKWISE),
	        new SwivelTransition2D(Transition2D.COUNTER_CLOCKWISE),
			new TossTransition2D(Transition2D.LEFT),
	        new TossTransition2D(Transition2D.RIGHT),
			new WaveTransition2D(Transition2D.UP),
	        new WaveTransition2D(Transition2D.RIGHT),
			new WeaveTransition2D(),
	    	new WipeTransition2D(Transition2D.LEFT),
	    	new WipeTransition2D(Transition2D.RIGHT),
	    	new WipeTransition2D(Transition2D.UP),
	    	new WipeTransition2D(Transition2D.DOWN),
	        new ZoomTransition2D(Transition2D.LEFT),
			new ZoomTransition2D(Transition2D.RIGHT)
    };
    
    public Transition2DDemo() {
    	super(transitions, false);
    }
}
