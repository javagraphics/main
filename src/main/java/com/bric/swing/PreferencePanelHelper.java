/*
 * @(#)PreferencePanelHelper.java
 *
 * $Date: 2014-05-04 18:08:30 +0200 (So, 04 Mai 2014) $
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
package com.bric.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.blog.BlogHelper;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.util.JVM;

public class PreferencePanelHelper extends BlogHelper {
	
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		Image[] icons = new Image[] {
				Toolkit.getDefaultToolkit().getImage("NSImage://NSBonjour"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSDotMac"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSComputer"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSFolderBurnable"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSFolderSmart"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSPreferencesGeneral"),
				Toolkit.getDefaultToolkit().getImage("NSImage://NSAdvanced")
		};
		final PreferencePanel panel = new PreferencePanel();

		AbstractButton[] buttons = new AbstractButton[icons.length];
		JComponent[] components = new JComponent[icons.length];
		for(int a = 0; a<icons.length; a++) {
			Icon icon;
			if(JVM.isMac==false) {
				if(a%4==0) {
					icon = UIManager.getIcon("OptionPane.informationIcon");
				} else if(a%4==1) {
					icon = UIManager.getIcon("OptionPane.warningIcon");
				} else if(a%4==2) {
					icon = UIManager.getIcon("OptionPane.questionIcon");
				} else {
					icon = UIManager.getIcon("OptionPane.errorIcon");
				}
			} else {
				icon = new ImageIcon(icons[a]);
			}
			buttons[a] = new JToggleButton(icon);
			buttons[a].setText("Button "+(a+1));
			components[a] = new JPanel();
			components[a].setBackground(new Color(220,220,220));
			components[a].setOpaque(true);
			buttons[a].setHorizontalTextPosition(SwingConstants.CENTER);
			buttons[a].setVerticalTextPosition(SwingConstants.BOTTOM);
			JLabel label = new JLabel(" ");
			label.setPreferredSize(new Dimension(300,300));
			components[a].add(label);
		}
		panel.addButtonRow(buttons, components, "");
		panel.setSize(panel.getPreferredSize());
		final JFrame frame = new JFrame();
		frame.getContentPane().add(panel);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				frame.pack();
			}
		});
		BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		panel.paint(g);
		g.dispose();
		return BasicThumbnail.getShadow(3).create(image, preferredSize);
	}
}
