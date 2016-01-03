/*
 * @(#)WindowMenuDemo.java
 *
 * $Date: 2015-03-01 07:58:11 +0100 (So, 01 Mär 2015) $
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
package com.bric.window;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bric.blog.Blurb;

/**
 * <P>This can't run inside a Java sandbox because it refers to the WindowList
 * which invokes <code>Toolkit.getDefaultToolkit().addAWTEventListener(..)</code>.
 */
@Blurb (
filename = "WindowMenu",
title = "Windows: Adding a Window Menu",
releaseDate = "November 2008",
summary = "This article discusses and emulates the \"Window\" menu found on Macs.",
link = "http://javagraphics.blogspot.com/2008/11/windows-adding-window-menu.html",
sandboxDemo = false
)
public class WindowMenuDemo {


	/** A simple demo app testing this menu. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		final JFrame f = new JFrame("Frame "+(Frame.getFrames().length+1));
		JMenuBar mb = new JMenuBar();
		mb.add(new JMenu("File"));
		mb.add(new JMenu("Edit"));
		mb.add(new WindowMenu(f));
		
		final JTextField titleField = new JTextField(f.getTitle());
		final JCheckBox modifiedBox = new JCheckBox("Window.documentModified");
		JButton newWindowButton = new JButton("New Window");
		newWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				main(null);
			}
		});
		f.setJMenuBar(mb);
		f.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(10,20,10,20);
		f.getContentPane().add(newWindowButton,c);
		//This would be useful to test bullets, but currently that
		//functionality is unimplemented.
		//c.gridy++;
		//f.getContentPane().add(modifiedBox,c);
		c.gridy++;
		f.getContentPane().add(titleField,c);
		titleField.setColumns(13);
		
		titleField.getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				f.setTitle(titleField.getText());
			}

			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}
		});
		
		modifiedBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.getRootPane().putClientProperty("Window.documentModified", new Boolean(modifiedBox.isSelected()));
			}
		});

        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.pack();
		f.setVisible(true);
	}
}
