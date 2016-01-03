/*
 * @(#)JThrobberDemo.java
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
package com.bric.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.bric.blog.Blurb;
import com.bric.plaf.AquaThrobberUI;
import com.bric.plaf.ChasingArrowsThrobberUI;
import com.bric.plaf.DetachingArcThrobberUI;
import com.bric.plaf.PivotingCirclesThrobberUI;
import com.bric.plaf.PulsingCirclesThrobberUI;
import com.bric.plaf.SierpinskiThrobberUI;
import com.bric.plaf.StarThrobberUI;
import com.bric.plaf.ThrobberUI;
import com.bric.util.JVM;

/** A simple demo for the <code>JThrobber</code> class.
 * <p>Here is a recording of this demo app in use:
 * <p><img src="https://javagraphics.java.net/resources/throbber.gif" alt="JThrobber Demo">
 * 
 */
@Blurb (
filename = "JThrobber",
title = "Implementing a JThrobber ",
releaseDate = "March 2014",
summary = "This introduces <a href=\"https://javagraphics.java.net/doc/com/bric/swing/JThrobber.html\">a new component</a> for "+
"showing asynchronous indeterminate feedback.\n"+
"<p>They're vector-based, so while they're originally designed to be less than 20x20 pixels: "+
"they scale well if you need them to. The article/demo app also include some fun non-standard "+
"alternative UIs (that were fun to design, but I don't necessarily recommend).",
instructions = "Move the mouse over each throbber to see a zoom-in view. Use the checkboxes at the top "+
		"of the panel to alter the playback/zoom.\n"+
		"<P>If you're viewing this on a Mac (and permissions allow): then on the far right you may see "+
		"a throbber that is actually Apple's implementation. (That is: that component is really a "+
		"<code>JProgressBar</code> whose client property \"JProgressBar.style\" is set to \"circular\".) "+
		"This is provided for comparison, but is a completely different component than all the others.",
link = "http://javagraphics.blogspot.com/2014/03/implementing-jthrobber.html",
sandboxDemo = true
)
public class JThrobberDemo extends BricApplet{

	private static final long serialVersionUID = 1L;
	public static final boolean isMac = System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1;
	
	public static void main(String[] args) {
		JThrobberDemo d = new JThrobberDemo();
		JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(d);
		frame.pack();
		frame.setVisible(true);
	}
	
	/** The optional real aqua UI */
	protected JProgressBar aquaIndicator;
	
	protected JPanel throbberContainer = new JPanel(new GridBagLayout());
	protected MagnificationPanel zoomPanel = new MagnificationPanel(throbberContainer, 20, 20, 12);
	
	/** This slows each spinning UI down to help in bug testing. */
	protected JCheckBox slowMode = new JCheckBox("Slow Mode",false);
	protected JCheckBox vectorZoom = new JCheckBox("Vector Zoom",false);
	protected JLabel label = new JLabel(" ");
	protected List<JThrobber> throbbers = new ArrayList<JThrobber>();
	
	public JThrobberDemo() {
		super();
			
		throbberContainer.setOpaque(false);
		addSampleUI(new ChasingArrowsThrobberUI());
		addSampleUI(new AquaThrobberUI());
		addSampleUI(new PulsingCirclesThrobberUI());
		addSampleUI(new DetachingArcThrobberUI());
		addSampleUI(new SierpinskiThrobberUI());
		addSampleUI(new StarThrobberUI());
		addSampleUI(new PivotingCirclesThrobberUI());

		if(JVM.isMac) {
			aquaIndicator = new JProgressBar();
			aquaIndicator.putClientProperty("JProgressBar.style", "circular");
			aquaIndicator.setIndeterminate(true);
			addSample(aquaIndicator, "Apple's \"JProgressBar.style\"");
		}
		
		getContentPane().setBackground(Color.white);
		getContentPane().setLayout(new GridBagLayout());
		
		slowMode.setOpaque(false);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.insets = new Insets(5,5,5,5);
		c.weightx = 1;
		c.weighty = 1; c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(slowMode,c);
		c.gridy++;
		getContentPane().add(vectorZoom,c);
		c.gridy++; c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
		getContentPane().add(throbberContainer,c);
		c.gridy++;
		getContentPane().add(label,c);
		c.weighty = 1; c.weightx = 1;
		c.gridy++; c.fill = GridBagConstraints.BOTH;
		getContentPane().add(zoomPanel,c);

		Timer timer = new Timer(20,new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomPanel.refresh();
			}
		});
		timer.start();
		
		zoomPanel.setInstruction("Drag Mouse Over Throbbers");
		
		slowMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSlowFactor( slowMode.isSelected() ? 4 : 1 );
			}
		});
		vectorZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				zoomPanel.setPixelated(!vectorZoom.isSelected());
			}
		});
	}
	
	protected void setSlowFactor(int factor) {
		for(Component c : throbberContainer.getComponents()) {
			if(c instanceof JThrobber) {
				((JThrobber)c).putClientProperty( ThrobberUI.PERIOD_MULTIPLIER_KEY, factor);
			}
		}
	}
	
	/** Add a JThrobber to the throbberContainer. */
	private void addSampleUI(ThrobberUI ui) {
		JThrobber throbber = new JThrobber();
		throbber.setUI(ui);
		throbbers.add(throbber);
		
		String name = ui.getClass().getName();
		name = name.substring(name.lastIndexOf('.')+1);
		addSample(throbber, name);
	}

	/** Add a JComponent to the throbberContainer. */
	private void addSample(JComponent jc,final String labelText) {
		int index = throbberContainer.getComponentCount();
		int row = index / 8;
		int column = index % 8;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = column; c.gridy = row;
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(4,4,4,4);
		throbberContainer.add(jc, c);
		
		jc.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				label.setText(labelText);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				label.setText(" ");
			}
		});
	}
}
