/*
 * @(#)ShapeTestApp.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.geom.tests;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.inspector.PropertyInspector;
import com.bric.util.ObservableList;
import com.bric.util.Property;

public class ShapeTestApp extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ShapeTestApp app = new ShapeTestApp();
				app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				app.setVisible(true);
			}
		});
	}
	
	ObservableList<Test> tests = new ObservableList<Test>();
	TestTable table = new TestTable();
	PropertyInspector propertyInspector = new PropertyInspector();
	Runnable updateEnabledState = new Runnable() {
		public void run() {
			Test test = table.getSelectedTest();
			if(test==null) {
				propertyInspector.setPropertiesEnabled(false);
			} else {
				propertyInspector.setPropertiesEnabled(!test.isRunning());
			}
		}
	};
	
	public ShapeTestApp() {
		super("Shape Test");
		tests.add(new ContainsPointTest());
		getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 0; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		getContentPane().add(new JScrollPane(table), c);
		
		c.gridx++;
		c.weightx = 1;
		getContentPane().add(propertyInspector, c);
		
		table.setTests( tests.toArray(new Test[tests.size()]) );
		
		table.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Test test = table.getSelectedTest();
				if(test==null) {
					propertyInspector.clear();
				} else {
					Property[] properties = test.getProperties();
					propertyInspector.setProperties(properties);
					SwingUtilities.invokeLater(updateEnabledState);
				}
			}
		});
		
		for(int a = 0; a<tests.size(); a++) {
			Test test = tests.get(a);
			test.addStateListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					SwingUtilities.invokeLater(updateEnabledState);
				}
			});
		}
		SwingUtilities.invokeLater(updateEnabledState);
		
		pack();
	}
}
