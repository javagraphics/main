/*
 * @(#)QOptionPaneDemo.java
 *
 * $Date: 2014-05-07 01:18:06 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class QOptionPaneDemo extends BricApplet {
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
				
				final JFrame frame = new JFrame("QOptionPane Demo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new QOptionPaneDemo());
				frame.pack();
				frame.setVisible(true);
				
				//this tests sheets:
				/*Thread delay = new Thread() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch(Exception e) {}
						QOptionPaneCommon.showSaveDialog(frame, "My Application", "Untitled", null, true, QOptionPaneCommon.FILE_NORMAL, true);
					}
				};
				delay.start();*/
			}
		});
	}
	
	protected static void showBatteryDialog(JFrame frame) {
		QOptionPane optionPane = new QOptionPane(
				"Change your battery or switch to outlet power immediately.", 
				"Your computer has a low battery, so you should act immediately to keep from losing your work.",
				QOptionPane.ICON_WARNING, 
				DialogFooter.OK_OPTION,
				"Warning"
				
			);
		optionPane.showDialog(frame, false);
	}
	
	JDesktopPane desktopPane = new JDesktopPane();
	
	public QOptionPaneDemo() {
		super();
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.weightx = 1; gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		getContentPane().add(desktopPane, gbc);
		
		desktopPane.setPreferredSize(new Dimension(500, 500));
		desktopPane.setBackground(new Color(0xcccccc));
		String appName = "My Application";
		String docName = "My Document";
		String filePath = System.getProperty("user.home")+File.separator+docName+".xyz";
		JInternalFrame f1 = QOptionPaneCommon.showSaveDialog(desktopPane, appName, docName, filePath, false, QOptionPaneCommon.FILE_EXTERNAL_CHANGES);
		JInternalFrame f2 = QOptionPaneCommon.showSaveDialog(desktopPane, appName, docName, filePath, true, QOptionPaneCommon.FILE_NORMAL);
		JInternalFrame f3 = QOptionPaneCommon.showReviewChangesDialog(desktopPane, "My Application", 3);
	
		cascade( new JInternalFrame[] {f1, f2, f3});
	}
	
	public static void cascade(JInternalFrame[] frames) {
		for(int a = 0; a<frames.length; a++) {
			frames[a].setLocation(30*a, 30*a);
			frames[a].toFront();
		}
	}
}
