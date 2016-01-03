/*
 * @(#)ViewerHeader.java
 *
 * $Date: 2014-03-23 07:01:48 +0100 (So, 23 Mär 2014) $
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

import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.io.location.IOLocation;
import com.bric.io.location.IOLocationFilter;
import com.bric.plaf.ButtonCluster;
import com.bric.swing.ContextualMenuHelper;
import com.bric.swing.JThrobber;
import com.bric.swing.PaddedIcon;
import com.bric.swing.io.LocationBreadCrumbs;
import com.bric.swing.io.LocationHistory;
import com.bric.swing.resources.TriangleIcon;

public class ViewerHeader extends JPanel {
	private static final long serialVersionUID = 1L;
	
	static class CurrentFilter extends IOLocationFilter {
		@Override
		public IOLocation filter(IOLocation loc) {
			return loc;
		}
	}
	
	static class CustomFilter extends IOLocationFilter {
		@Override
		public IOLocation filter(IOLocation loc) {
			return loc;
		}
	}
	
	public static final String URL_KEY = ViewerHeader.class.getName()+".url";

	JThrobber throbber = new JThrobber();
	JTextField urlField = new JTextField(10);
	LocationBreadCrumbs breadCrumbs;
	
	JButton prevButton = new JButton(new PaddedIcon(new TriangleIcon(SwingConstants.WEST, 8, 8), 4));
	JButton nextButton = new JButton(new PaddedIcon(new TriangleIcon(SwingConstants.EAST, 8, 8), 4));
	JToolBar navToolbar = new JToolBar();
	ViewerApplet viewer;
	
	static class OpaqueBorder implements Border {
		Color color;
		int left, right, top, bottom;
		public OpaqueBorder(Color color,int insets) {
			this.color = color;
			left = insets;
			right = insets;
			top = insets;
			bottom = insets;
		}
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(color);
			g.fillRect(x, y, c.getWidth(), c.getHeight());
			
		}
		public Insets getBorderInsets(Component c) {
			return new Insets(top,left, bottom, right);
		}
		public boolean isBorderOpaque() {
			return true;
		}
	}
	
	public ViewerHeader(ViewerApplet viewer) {
		setLayout(new GridBagLayout());
		this.viewer = viewer;
		breadCrumbs = new LocationBreadCrumbs(viewer.locHistory);
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5,5,5,5);
		add(navToolbar, c);
		c.gridx++; c.weightx = 1;
		add(urlField, c);
		add(breadCrumbs, c);
		c.gridy--;
		c.gridx++; c.weightx = 0;
		add(viewer.filter.filterMenuTrigger, c);
		c.gridx++;
		add(throbber, c);
		
		breadCrumbs.setBorder(new CompoundBorder(urlField.getBorder(), new EmptyBorder(2, 2, 2, 2)));
		breadCrumbs.setOpaque(true);
		breadCrumbs.setBackground(Color.white);
		
		throbber.setVisible(false);
		navToolbar.add(prevButton);
		navToolbar.add(nextButton);
		navToolbar.setFloatable(false);
		
		ButtonCluster.install(navToolbar, ViewerApplet.buttonUI, false);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				urlField.requestFocus();
				updateControls();
			}
		});
		
		ContextualMenuHelper.add(urlField, "Browse...", new Runnable() {
			public void run() {
				doBrowseForFile();
			}
		});
		
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewerHeader.this.viewer.locHistory.next();
			}
		});
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ViewerHeader.this.viewer.locHistory.back();
			}
		});
		
		urlField.getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String s = urlField.getText();
						try {
							URL url = new URL(s);
							setURL(url);
						} catch(MalformedURLException e) {
							File f = new File(s);
							setURL(f);
						}
					}
				});
			}

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
			
		});
	}
	
	public File doBrowseForFile() {
		Window w = SwingUtilities.getWindowAncestor(this);
		FileDialog fd = new FileDialog( (Frame)w );
		fd.pack();
		fd.setLocationRelativeTo(null);
		fd.setVisible(true);
		
		if(fd.getFile()==null) return null;
		File f = new File(fd.getDirectory()+fd.getFile());
		setURL(f);
		return f;
	}
	
	public void setURL(File file) {
		try {
			setURL(file.toURI().toURL());
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public URL getURL() {
		return (URL)getClientProperty(URL_KEY);
	}
	
	public void setSpinnerVisible(boolean b) {
		throbber.setVisible(b);
	}
	
	public boolean isSpinnerVisible() {
		return throbber.isVisible();
	}
	
	public boolean setURL(URL url) {
		URL oldURL = getURL();
		if(oldURL!=null && oldURL.equals(url)) {
			return false;
		}
		putClientProperty(URL_KEY, url);
		String text = url==null ? "" : url.toString();
		if(text.equals(urlField.getText()))
			return false;
		urlField.setText(text);
		
		return true;
	}

	public void updateControls() {
		LocationHistory locHistory = viewer.locHistory;
		IOLocation loc = locHistory.getLocation();
		URL url = loc==null ? null : loc.getURL();
		if(url!=null) {
			setURL(url);
		}

		breadCrumbs.setVisible(url==null);
		urlField.setVisible(url!=null);
		
		nextButton.setEnabled(locHistory.hasNext());
		prevButton.setEnabled(locHistory.hasBack());
		
		
	}
}
