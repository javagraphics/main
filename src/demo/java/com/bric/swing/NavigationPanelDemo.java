/*
 * @(#)NavigationPanelDemo.java
 *
 * $Date: 2015-12-26 08:54:45 +0100 (Sa, 26 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.plaf.CompactNavigationPanelUI;

public class NavigationPanelDemo extends BricApplet {

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
				f.getContentPane().add(new NavigationPanelDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	
	NavigationModel model = new NavigationModel("Page", 50,100);
	NavigationPanel navPanel1 = new NavigationPanel(model);
	NavigationPanel navPanel2 = new NavigationPanel(model);
	
	public NavigationPanelDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(5,5,5,5);
		getContentPane().add(navPanel1, c);
		c.gridy++;
		getContentPane().add(navPanel2, c);
		
		navPanel2.setUI(new CompactNavigationPanelUI());
	}
	
}
