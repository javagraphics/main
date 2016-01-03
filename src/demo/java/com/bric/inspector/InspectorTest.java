/*
 * @(#)InspectorTest.java
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
package com.bric.inspector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import com.bric.blog.Blurb;
import com.bric.debug.DebugPanel;
import com.bric.swing.BricApplet;

/** FIXME: this does not launch, and is outdated.
 */
@Blurb (
filename = "Inspector",
title = "Layouts: Designing an Inspector",
releaseDate = "June 2009",
summary = "This article examines the notion of a well-designed inspector.\n"+
"<p>It uses <a href=\"https://javagraphics.java.net/doc/com/bric/inspector/InspectorLayout.html\">this interface</a> to "+
" layout the components, and it presents a few implementations "+
"of that interface (with varying degrees of success/hackery).",
link = "http://javagraphics.blogspot.com/2009/06/layouts-designing-inspector.html",
sandboxDemo = true
)
public class InspectorTest extends BricApplet {
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
				
				InspectorTest t = new InspectorTest();
				JFrame f = new JFrame("Inspector Test");
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(t);
				f.pack();
				f.setVisible(true);
				
				if(InspectorGridBagLayout.isMac==false) {
					JOptionPane.showMessageDialog(f, "This demo is primarily targeted to Macs.\nThere may not be a noticeable difference in the GBL and AGBL tabs\non non-Mac platforms.  Also in Vista there are several rendering bugs.", "Notice", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
	
	JTabbedPane tabs = new JTabbedPane();
	
	public InspectorTest() {
		getContentPane().add(tabs);
		
		JPanel panel1 = new DebugPanel();
		JPanel panel3 = new DebugPanel();
		InspectorLayout layout1 = new InspectorGridBagLayout(panel1);
		InspectorLayout layout3 = new InspectorGroupLayout(panel3);
		
		populate(layout1);
		populate(layout3);
		
		tabs.add(panel1,"GBL Layout");
		tabs.add(panel3,"Group Layout");
		
		standardize(panel1);
		standardize(panel3);
		
		if(getContentPane() instanceof JComponent)
			((JComponent)getContentPane()).setOpaque(true);
		getContentPane().setBackground(Color.white);
	}
	
	protected void standardize(JPanel panel) {
		for(int a = 0; a<panel.getComponentCount(); a++) {
			JComponent c = (JComponent)panel.getComponent(a);
			c.putClientProperty("JComponent.sizeVariant", "small");
			if(c instanceof JComboBox) {
				c.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
			} else if(c instanceof JSpinner) {
				Dimension d = c.getPreferredSize();
				d.width += 20;
				c.setPreferredSize(d);
			} else if(c instanceof JButton) {
				c.putClientProperty("JButton.buttonType","roundRect");
			}
			c.setOpaque(false);
			if(c instanceof JPanel)
				standardize((JPanel)c);
		}
		panel.setOpaque(false);
	}
	
	protected void populate(InspectorLayout l) {
		//new row
		JTextField tf = new JTextField("Charles Dickens",15);
		l.addRow(new JLabel("Name:"),tf,true);

		//new row
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Male");
		comboBox.addItem("Female");
		l.addRow(new JLabel("Gender:"),comboBox,false,new JButton("Other"));
		
		l.addSeparator();

		//new row
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(5.8,4,8,.1));
		l.addRow(new JLabel("Height (ft):"),spinner,false);

		//new row
		l.addRow(new JCheckBox("Talent:"), new JSlider(0,100,98),true);

		//new row
		JPanel colorSwatch = new JPanel();
		colorSwatch.setPreferredSize(new Dimension(15,15));
		colorSwatch.setBorder(new CompoundBorder(new LineBorder(Color.black),new LineBorder(Color.white)));
		l.addRow(new JCheckBox("Favorite Color:"),colorSwatch,false);
		
		//new row:
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setOpaque(false);
		JButton hat1 = new JButton(new ImageIcon(new BufferedImage(14,14,BufferedImage.TYPE_INT_ARGB)));
		JButton hat2 = new JButton(new ImageIcon(new BufferedImage(14,14,BufferedImage.TYPE_INT_ARGB)));
		JButton hat3 = new JButton(new ImageIcon(new BufferedImage(14,14,BufferedImage.TYPE_INT_ARGB)));
		hat1.putClientProperty("JButton.buttonType","segmented");
		hat2.putClientProperty("JButton.buttonType","segmented");
		hat3.putClientProperty("JButton.buttonType","segmented");
		hat1.putClientProperty("JButton.segmentPosition","first");
		hat2.putClientProperty("JButton.segmentPosition","middle");
		hat3.putClientProperty("JButton.segmentPosition","last");
		hat1.setOpaque(false);
		hat2.setOpaque(false);
		hat3.setOpaque(false);
		toolbar.add(hat1);
		toolbar.add(hat2);
		toolbar.add(hat3);
		hat1.setToolTipText("(Pretend there are icons of hats here.)");
		l.addRow(new JLabel("Hat Type:"),toolbar,false,new JButton("More"));
		
		l.addGap();
	}
}
