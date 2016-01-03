/*
 * @(#)ViewerFooter.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.bric.plaf.AquaListLocationBrowserUI;
import com.bric.plaf.AquaTileLocationBrowserUI;
import com.bric.plaf.ButtonCluster;
import com.bric.plaf.ListLocationBrowserUI;
import com.bric.plaf.LocationBrowserUI;
import com.bric.plaf.TileLocationBrowserUI;
import com.bric.swing.io.LocationBrowser;
import com.bric.swing.resources.ListIcon;
import com.bric.swing.resources.StackIcon;
import com.bric.swing.resources.TileIcon;
import com.bric.viewer.plaf.SlideshowLocationBrowserUI;

public class ViewerFooter extends JPanel {
	private static final long serialVersionUID = 1L;

	
	JButton listView = new JButton(new ListIcon(12, 12));
	JButton thumbnailView = new JButton(new TileIcon(12, 12));
	JButton slideshowView = new JButton(new StackIcon(12, 12));
	JToolBar viewToolbar = new JToolBar();
	ViewerApplet viewer;
	
	public ViewerFooter(ViewerApplet viewer) {
		setLayout(new GridBagLayout());
		this.viewer = viewer;
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(5,5,5,5);
		add(viewToolbar, c);

		viewToolbar.add(thumbnailView);
		viewToolbar.add(listView);
		viewToolbar.add(slideshowView);
		viewToolbar.setFloatable(false);
		
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LocationBrowser browser = ViewerFooter.this.viewer.body;
				if(e.getSource()==thumbnailView) {
					browser.setUI(new AquaTileLocationBrowserUI(browser));
				} else if(e.getSource()==listView) {
					browser.setUI(new AquaListLocationBrowserUI(browser));
				} else if(e.getSource()==slideshowView) {
					browser.setUI(new SlideshowLocationBrowserUI(browser));
				}
				updateControls();
			}
		};
		thumbnailView.addActionListener(actionListener);
		listView.addActionListener(actionListener);
		slideshowView.addActionListener(actionListener);

		ButtonCluster.install(viewToolbar, ViewerApplet.buttonUI, false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateControls();
			}
		});
	}
	
	protected void updateControls() {
		LocationBrowserUI ui = viewer.body.getUI();
		thumbnailView.setSelected(ui instanceof TileLocationBrowserUI);
		listView.setSelected(ui instanceof ListLocationBrowserUI);
		slideshowView.setSelected(ui instanceof SlideshowLocationBrowserUI);
	}

}
