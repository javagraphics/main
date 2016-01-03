/*
 * @(#)CustomizedToolbarApp.java
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
package com.bric.swing.toolbar;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;

import com.bric.blog.Blurb;
import com.bric.util.JVM;

/** A demo app for the {@link CustomizedToolbar}.
 *
 */
@Blurb (
filename = "CustomizedToolbar",
title = "Customize Toolbar: Implementing Mac-Like Toolbars",
releaseDate = "June 2008",
summary = "In lots of Apple's software there's a handy menu option called \"Customize Toolbar\" that "+
"lets users drag and drop components in a toolbar.  It's functional <i>and</i> great eye candy.\n"+
"<p>This article presents a similar mechanism for Java.  A single thumbnail doesn't do justice to this feature, but "+
"if you go to the article you'll see a screencast.",
link = "http://javagraphics.blogspot.com/2008/06/customize-toolbar-implementing-mac-like.html",
sandboxDemo = true
)
public class CustomizedToolbarApp {

	/** A simple demo program. 
     * @param args the application's arguments. (This is unused.)
	 */
	public static void main(String[] args) {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		JButton customize = new JButton("Customize...");
		//these are the components we [may] display in the toolbar:
		JComponent[] list = new JComponent[] {
				customize,
				new JCheckBox("Check box"),
				new JLabel("Label"),
				new JButton("Button"),
				new JSlider()
		};
		
		for(int a = 0; a<list.length; a++) {
			list[a].setName( ""+a ); //give every component a unique name
			list[a].setOpaque(false);
		}
		
		JMenu viewMenu = new JMenu("View");
		JMenuItem customizeItem = new JMenuItem("Customize Toolbar...");
		viewMenu.add(customizeItem);
		
		final CustomizedToolbar tb = new CustomizedToolbar(list,new String[] {"0","\t","1"},"toolbar demo");
		
		ActionListener showCustomizeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tb.displayDialog( JVM.isMac ? 350 : 280);
			}
		};
		customize.addActionListener(showCustomizeAction);
		customizeItem.addActionListener(showCustomizeAction);
		
		JFrame f = new JFrame();
		f.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
		JMenuBar mb = new JMenuBar();
		mb.add(viewMenu);
		f.setJMenuBar(mb);
		f.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		f.getContentPane().add(tb,c);
		c.gridy++; c.weighty = 1;
		JPanel fluff = new JPanel();
		fluff.setOpaque(false);
		f.getContentPane().add(fluff,c);
		f.setSize(new Dimension(500,400));
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
