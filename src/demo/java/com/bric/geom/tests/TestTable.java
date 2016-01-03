/*
 * @(#)TestTable.java
 *
 * $Date: 2014-05-07 01:28:23 +0200 (Mi, 07 Mai 2014) $
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.plaf.FilledButtonUI;
import com.bric.plaf.RoundRectButtonUI;
import com.bric.swing.resources.PauseIcon;
import com.bric.swing.resources.TriangleIcon;

public class TestTable extends JPanel {
	private static final long serialVersionUID = 1L;

	Vector<ChangeListener> changeListeners = new Vector<ChangeListener>();
	Test selectedTest = null;
	
	public TestTable() {
		super(new GridBagLayout());
	}
	
	public void addChangeListener(ChangeListener l) {
		if(changeListeners.contains(l))
			return;
		changeListeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}
	
	protected void fireChangeListeners() {
		for(int a = 0; a<changeListeners.size(); a++) {
			ChangeListener l = changeListeners.get(a);
			try {
				l.stateChanged(new ChangeEvent(this));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setSelectedTest(Test test) {
		if(test==selectedTest)
			return;
		selectedTest = test;
		fireChangeListeners();
	}
	
	public Test getSelectedTest() {
		return selectedTest;
	}
	
	public void setTests(Test[] tests) {
		removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1; c.weighty = 0;
		
		for(int a = 0; a<tests.length; a++) {
			TestRow row = new TestRow(tests[a]);
			c.gridy++;
			add(row, c);
		}
		c.gridy++;
		c.weighty = 1;
		JPanel fluff = new JPanel();
		fluff.setOpaque(false);
		add(fluff, c);
	}
	
	class TestRow extends JPanel {
		private static final long serialVersionUID = 1L;

		Test test;
		JLabel label = new JLabel();
		JProgressBar progressBar = new JProgressBar();
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				setSelectedTest(test);
			}
		}; 
		PlayButton playButton = new PlayButton();
		
		public TestRow(Test t) {
			super(new GridBagLayout());
			this.test = t;

			label.setText(test.getName());
			
			GridBagConstraints c = new GridBagConstraints();
			
			c.insets = new Insets(3,3,3,3);
			c.anchor = GridBagConstraints.WEST;
			
			c.gridx = 0; c.gridy = 0;
			c.weightx = 1; c.weighty = 0;
			c.fill = GridBagConstraints.NONE;
			add(label, c);
			
			c.gridx++;
			add(progressBar, c);

			test.getCompletion().addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					float f = test.getCompletion().getValue();
					int range = progressBar.getMaximum()-progressBar.getMinimum();
					progressBar.setValue( progressBar.getMinimum()+(int)(f*range) );
				}
			});
				
			test.addStateListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					playButton.setPlaying(test.isRunning());
				}
			});
			playButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					test.setPaused(!playButton.isPlaying());
				}
			});
			c.weightx = 0;
			c.gridx++;
			add(playButton, c);
				
			label.addMouseListener(mouseListener);
			progressBar.addMouseListener(mouseListener);
		}
	}
}

class PlayButton extends JToggleButton {
	private static final long serialVersionUID = 1L;

	public static final Icon playIcon = new TriangleIcon(SwingConstants.EAST, 6, 6);
	public static final Icon pauseIcon = new PauseIcon(6, 6);
	
	public PlayButton() {
		this.setSelectedIcon(playIcon);
		this.setIcon(pauseIcon);
		RoundRectButtonUI ui = new RoundRectButtonUI();
		this.putClientProperty(FilledButtonUI.SHAPE, new Ellipse2D.Float(0,0,100,100));
		setUI(ui);
	}
	
	public void setPlaying(boolean b) {
		setSelected(b);
	}
	
	public boolean isPlaying() {
		return isSelected();
	}
}
