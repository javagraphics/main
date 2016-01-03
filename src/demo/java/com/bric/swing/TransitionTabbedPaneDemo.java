/*
 * @(#)TransitionTabbedPaneDemo.java
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
package com.bric.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.blog.Blurb;
import com.bric.image.transition.DropTransition2D;
import com.bric.image.transition.PushTransition2D;
import com.bric.image.transition.ScribbleTransition2D;
import com.bric.image.transition.Transition;
import com.bric.image.transition.Transition2D;

/** A simple demo of the {@link TransitionTabbedPane}.
 * 
 */
@Blurb (
filename = "TransitionTabbedPane",
title = "Transitions: Apply Transitions to a JTabbedPane",
releaseDate = "January 2009",
summary = "This applies some basic <code>Transition2D's</code> to a <code>JTabbedPane</code> when you change tabs.",
instructions = "This applet demonstrates the <code>TransitionTabbedPane</code>.\n"+
	"<p>As you click tabs: the UI updates with a <code>Transition2D</code>. The spinner at the top of the panel "+
	"controls the duration. You could use any transition here, but I only provided a few presets in the JComboBox below.",
scrapped = "This was made at someone's request, but I never liked the idea of a JTabbedPane that animates. "+
	"(I'm much more pleased with the "+
	"<a href=\"http://javagraphics.blogspot.com/2014/03/panels-refreshes-and-transitions.html\">TransitionPanel</a>.) "+
	"Also this is buggy: when the applet is viewed in Firefox the tabbed panes darken during animation playback.",
	sandboxDemo = true
)
public class TransitionTabbedPaneDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	/** A simple demo of the TransitionTabbedPane. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.getContentPane().add(new TransitionTabbedPaneDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	TransitionTabbedPane tabs = new TransitionTabbedPane() {
		private static final long serialVersionUID = 1L;

		@Override
		protected Transition2D getTransition2D(int prevSelectedIndex,
				int newSelectedIndex) {
			int i = choices.getSelectedIndex();
			if(i==1) {
				return new PushTransition2D(Transition.DOWN);
			} else if(i==2) {
				return new ScribbleTransition2D(false);
			} else if(i==3) {
				return new DropTransition2D();
			}
			
			return super.getTransition2D(prevSelectedIndex, newSelectedIndex);
		}
	};
	
	JComboBox choices = new JComboBox();
	
	public TransitionTabbedPaneDemo() {

		choices.addItem("Wipe");
		choices.addItem("Push");
		choices.addItem("Scribble");
		choices.addItem("Drop");
		JPanel[] components = new JPanel[] {
				new JPanel(new GridBagLayout()),
				new JPanel(new GridBagLayout()),
				new JPanel(new GridBagLayout())
		};
		
		Random random = new Random(0);
		for(int a = 0; a<components.length; a++) {
			components[a].setOpaque(false);
			tabs.add(components[a],"Tab "+a);
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST; c.insets = new Insets(3,3,3,3);
			c.weightx = 1; c.weighty = 1;
			for(int x = 0; x<2; x++) {
				for(int y = 0; y<10; y++) {
					c.gridx = x; c.gridy = y;
					int r = random.nextInt(5);
					JComponent newGuy = null;
					if(r==0) {
						newGuy = new JCheckBox("Check Box");
					} else if(r==1) {
						newGuy = new JSpinner();
					} else if(r==2) {
						newGuy = new JRadioButton("Radio Button");
					} else if(r==3) {
						newGuy = new JSlider();
					} else if(r==4) {
						newGuy = new JButton("Button");
					}
					components[a].add(newGuy,c);
					newGuy.setOpaque(false);
				}
			}
		}
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.insets = new Insets(3,3,3,3);
		c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.EAST;
		getContentPane().add(new JLabel("Duration:"),c);
		JSpinner duration = new JSpinner(new SpinnerNumberModel(.750,.5,5,.25));
		c.gridx++;  c.anchor = GridBagConstraints.WEST;
		getContentPane().add(duration,c);
		c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST;
		getContentPane().add(new JLabel("Transition:"),c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		getContentPane().add(choices,c);
		c.gridx = 0; c.gridy++; c.gridwidth = 2; c.weighty = 1;
		getContentPane().add(tabs,c);

		getContentPane().setBackground(Color.white);
		
		duration.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner)e.getSource();
				Number n = (Number)s.getValue();
				tabs.setTransitionDuration(n.floatValue());
			}
		});
	}
}
