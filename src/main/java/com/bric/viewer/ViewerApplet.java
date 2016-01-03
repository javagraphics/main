/*
 * @(#)ViewerApplet.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
package com.bric.viewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.io.location.IOLocation;
import com.bric.io.location.LocationFactory;
import com.bric.plaf.AquaTileLocationBrowserUI;
import com.bric.plaf.FilledButtonUI;
import com.bric.plaf.GradientButtonUI;
import com.bric.swing.BricApplet;
import com.bric.swing.io.GraphicCache;
import com.bric.swing.io.IOSelectionModel;
import com.bric.swing.io.LocationBrowser;
import com.bric.swing.io.LocationHistory;

public class ViewerApplet extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ViewerApplet d = new ViewerApplet();
				JFrame f = new JFrame();
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(d);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	static FilledButtonUI buttonUI = new GradientButtonUI();
	
	ViewerHeader header;
	ViewerFooter footer;
	LocationHistory locHistory = new LocationHistory();
	LocationBrowser body = new LocationBrowser( new IOSelectionModel(false), locHistory, new GraphicCache());
	ViewerFilter filter;
	
	public ViewerApplet() {
		filter = new ViewerFilter();
		header = new ViewerHeader(this);
		footer = new ViewerFooter(this);
		setLayout(new GridBagLayout());
		body.setUI(new AquaTileLocationBrowserUI(body));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add( header, c);
		c.fill = GridBagConstraints.BOTH;
		c.gridy++; c.weighty = 1;
		JScrollPane scrollPane = new JScrollPane(body);
		add( new JScrollPane(body), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy++; c.weighty = 0;
		add( footer, c);
		
		scrollPane.setPreferredSize(new Dimension(800, 600));
		
		header.addPropertyChangeListener( ViewerHeader.URL_KEY, new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent evt) {
				URL url = (URL)evt.getNewValue();
				IOLocation loc = LocationFactory.get().create(url);
				locHistory.append(loc);
			}
		});
		
		locHistory.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						updateControls();
					}
				});
			}
		});
		
		locHistory.append(LocationFactory.get().create(new File(System.getProperty("user.home"))));
		
		ChangeListener filterListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				body.setFilter(filter.getFilter());
			}
		};
		filter.addChangeListener(filterListener);
		filterListener.stateChanged(null);
		
		updateControls();
	}
	
	protected void updateControls() {
		header.updateControls();
	}
}
