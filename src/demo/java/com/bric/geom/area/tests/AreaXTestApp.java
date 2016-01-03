/*
 * @(#)AreaXTestApp.java
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
package com.bric.geom.area.tests;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bric.io.ConsoleLogger;
import com.bric.math.MathG;
import com.bric.util.ObservableList;

public class AreaXTestApp extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		MathG.acos(0); //jog this into existence so it doesn't affect profiles
		System.setProperty("apple.awt.graphics.UseQuartz", "false");
		
		ConsoleLogger logger = new ConsoleLogger("AreaX Test App.txt");
		logger.setLoggingSystemErr(true);
		logger.setLoggingSystemOut(true);
		
		AreaXTestApp app = new AreaXTestApp();
		app.setVisible(true);
	}
	
	ObservableList<TestElement> tests = new ObservableList<TestElement>();
	JList list = new JList(tests.getListModelEDTMirror());
	JComponent leftComponent = new JScrollPane(list);
	JPanel rightComponent = new JPanel(new GridBagLayout());
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
	
	ListSelectionListener listSelectionListener = new ListSelectionListener() {
		TestElement lastTest = null;
		
		public void valueChanged(ListSelectionEvent e) {
			TestElement test = (TestElement)list.getSelectedValue();
			if(test!=lastTest) {
				rightComponent.removeAll();
				if(lastTest!=null) {
					lastTest.cancel();
				}
				if(test!=null) {
					GridBagConstraints c = new GridBagConstraints();
					c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
					c.fill = GridBagConstraints.BOTH;
					rightComponent.add(test.getComponent(), c);
				}
				lastTest = test;
			}
			rightComponent.revalidate();
			rightComponent.invalidate();
			rightComponent.repaint();
		}
		
	};
	
	public AreaXTestApp() {
		super("AreaX Tests");
		
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				value = ((TestElement)value).getName();
				return super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
		});
		tests.add(new AccuracyTest());
		tests.add(new PerformanceTest());
		tests.add(new TransformTest());
		//tests.add(new IntersectionsTest());
		tests.add(new RelationshipTest());
		tests.add(new AddRulesTest());
		
		list.addListSelectionListener(listSelectionListener);
		list.setSelectedIndex(0);
		
		getContentPane().add(splitPane);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}
}
