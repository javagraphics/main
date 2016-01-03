/*
 * @(#)InspectorLayoutDemo.java
 *
 * $Date: 2014-05-09 15:15:15 +0200 (Fr, 09 Mai 2014) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.awt.RowLayout;
import com.bric.awt.RowLayout.Cell;
import com.bric.awt.RowLayout.ComponentCluster;
import com.bric.awt.RowLayout.ComponentConstraints;
import com.bric.swing.InspectorLayout.Header;
import com.bric.swing.InspectorLayout.HorizontalAlignment;

/**
 * 
 * 
 */
public class InspectorLayoutDemo extends BricApplet {
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
				
				JFrame f = new JFrame();
				f.getContentPane().add(new InspectorLayoutDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	InspectorLayout layout = new InspectorLayout();
	JComboBox headersComboBox = new JComboBox(new String[] {"Opaque", "Transparent", "Hidden"});
	JComboBox columnsComboBox = new JComboBox(new String[] {"Sectional", "Uniform"});
	Set<Header> headers = new HashSet<Header>();
	
	public InspectorLayoutDemo() {
		JRadioButton debugOnButton = new JRadioButton("On");
		JRadioButton debugOffButton = new JRadioButton("Off", true);
		ButtonGroup activeGroup = new ButtonGroup();
		activeGroup.add(debugOnButton);
		activeGroup.add(debugOffButton);
		
		JRadioButton alignOnButton = new JRadioButton("Active", true);
		JRadioButton alignOffButton = new JRadioButton("Inactive");
		ButtonGroup alignGroup = new ButtonGroup();
		alignGroup.add(alignOnButton);
		alignGroup.add(alignOffButton);
		
		layout.setHorizontalEdgePainted(false);
		headers.add( layout.addHeader("Real Controls", false) );
		layout.addRow( new JLabel("Headers:"),  headersComboBox, HorizontalAlignment.LEFT, null);
		ComponentCluster alignmentControls = new ComponentCluster(
				new JComponent[] { alignOnButton, alignOffButton, null },
				new ComponentConstraints[] { new ComponentConstraints(0), new ComponentConstraints(0), new ComponentConstraints(1) });
		layout.addRow(new JLabel("Autoalign:"), alignmentControls);
		layout.addRow(new JLabel("Columns:"), columnsComboBox, HorizontalAlignment.LEFT, null);
		ComponentCluster debugControls= new ComponentCluster(
			new JComponent[] { debugOnButton, debugOffButton, null },
			new ComponentConstraints[] { new ComponentConstraints(0), new ComponentConstraints(0), new ComponentConstraints(1) });
		layout.addRow(new JLabel("Debug View:"), debugControls);
		headers.add( layout.addHeader("Name", true) );
		JComboBox namePrefix = new JComboBox(new String[] { "Mr.", "Mrs.", "Ms.", "Dr.", "Lt."});
		layout.addRow(new JLabel("Full Name:"), new JTextField(10), HorizontalAlignment.STRETCH, null);
		layout.addRow(new JCheckBox("Prefix:"), namePrefix, HorizontalAlignment.LEFT, null);
		layout.addRow(new JCheckBox("Suffix:"), new JTextField(3), HorizontalAlignment.LEFT, null);
		headers.add( layout.addHeader("Personal Details", true) );
		JSpinner heightSpinner = new JSpinner( new SpinnerNumberModel(5.6, 2, 8, .5));
		layout.addRow(new JLabel("Height:"), heightSpinner, HorizontalAlignment.LEFT, null);
		layout.addRow(new JLabel("Attractiveness:"), new JSlider(), HorizontalAlignment.STRETCH, null);
		layout.addGap();
		
		headersComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(Header h : headers) {
					if(headersComboBox.getSelectedIndex()==0) {
						h.setVisible(true);
						h.setOpaque(true);
					} else if(headersComboBox.getSelectedIndex()==1) {
						h.setVisible(true);
						h.setOpaque(false);
					} else if(headersComboBox.getSelectedIndex()==2) {
						h.setVisible(false);
					}
					h.repaint();
				}
			}
		});
		
		columnsComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RowLayout rl = layout.getRowLayout();
				int sectionCtr = 0;
				for(int a = 0; a<rl.getRowCount(); a++) {
					JComponent[] c = rl.getRow(a);
					if(c.length==1 && c[0] instanceof Header) {
						sectionCtr++;
						
						//Note: this exact naming convention/definition
						//already exists in the InspectorLayout, so calling
						//RowLayout.addRowType is redundant. It's provided
						//here for instructional purposes, though:
						
						Cell[] cells = new Cell[] {
								new Cell(0, .5f, new Insets(1,5,1,5)),
								new Cell(1, .5f, new Insets(1,5,1,5) )
						};
						rl.addRowType(InspectorLayout.TWO_COLUMN_INSPECTOR+"."+sectionCtr, cells);
					} else {
						if(columnsComboBox.getSelectedIndex()==0) {
							rl.setRowType(a, InspectorLayout.TWO_COLUMN_INSPECTOR+"."+sectionCtr);
						} else {
							rl.setRowType(a, InspectorLayout.TWO_COLUMN_INSPECTOR);
						}
					}
				}
			}
		});
		
		debugOnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layout.getRowLayout().debug = true;
				layout.getPanel().repaint();
			}
		});
		debugOffButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layout.getRowLayout().debug = false;
				layout.getPanel().repaint();
			}
		});
		alignOnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layout.getRowLayout().setDefaultAutoAlign(true);
			}
		});
		alignOffButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layout.getRowLayout().setDefaultAutoAlign(false);
			}
		});
		
		Dimension d = heightSpinner.getPreferredSize();
		d.width += 20;
		heightSpinner.setPreferredSize(d);
		getContentPane().add(layout.getPanel());
	}
}
