/*
 * @(#)DockIconTest.java
 *
 * $Date: 2015-03-01 07:59:53 +0100 (So, 01 Mär 2015) $
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
package com.bric.mac;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import com.bric.blog.Blurb;
import com.bric.image.ImageLoader;

/** This class tests/demonstrates the <code>com.bric.mac.DockIcon</code> class.
 * 
 */
@Blurb (
filename = "DockIcon",
title = "Dock: Animating the Dock Icon",
releaseDate = "TBA",
summary = "This wraps calls to animate the dock icon for an app "+
"in reflection so it won't cause problems on non-Mac platforms.\n"+
"<p>Edit: although this is several years old, to my surprise it still "+
"worked. (March 2014)",
scrapped = "It's hard to write a whole blog article around this.",
sandboxDemo = false
)
public class DockIconTest {

	public static void main(String[] args) {
		try {
			//List these individually so the automated jar builder
			//knows to add them to the exported jar.
			String[] imageNames = new String[] {
					"resources/icon01.PNG",
					"resources/icon02.PNG",
					"resources/icon03.PNG",
					"resources/icon04.PNG",
					"resources/icon05.PNG",
					"resources/icon06.PNG",
					"resources/icon07.PNG",
					"resources/icon08.PNG",
					"resources/icon09.PNG",
					"resources/icon10.PNG",
					"resources/icon11.PNG",
					"resources/icon12.PNG",
					"resources/icon13.PNG",
					"resources/icon14.PNG",
					"resources/icon15.PNG",
					"resources/icon16.PNG",
					"resources/icon17.PNG",
					"resources/icon18.PNG",
					"resources/icon19.PNG",
					"resources/icon20.PNG"
			};
			final BufferedImage[] images = new BufferedImage[imageNames.length];
			final JCheckBox checkBox = new JCheckBox("Animate Dock Icon",true);
			for(int a = 0; a<images.length; a++) {
				images[a] = ImageLoader.createImage( DockIconTest.class.getResource( imageNames[a] ) );
			}
			JFrame f = new JFrame("Empty");
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(5,5,5,5);
			c.gridx = 0; c.gridy = 0;
			f.getContentPane().add(new JLabel("If this works, the icon in the dock will pulse."),c);
			c.gridy++;
			f.getContentPane().add(checkBox,c);
			f.pack();
			f.setVisible(true);
			
			
			Timer timer = new Timer(50,new ActionListener() {
				int ctr = 0;
				public void actionPerformed(ActionEvent e) {
					if(checkBox.isSelected()) {
						DockIcon.set(images[ctr]);
					} else {
						DockIcon.set(null);
					}
					
					ctr++;
					if(ctr>=images.length)
						ctr = 0;
				}
			});
			timer.start();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
