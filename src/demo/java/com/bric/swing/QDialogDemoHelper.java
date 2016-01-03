/*
 * @(#)QDialogDemoHelper.java
 *
 * $Date: 2014-05-07 01:18:06 +0200 (Mi, 07 Mai 2014) $
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.BlogHelper;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.swing.DialogFooter.EscapeKeyBehavior;

public class QDialogDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		//the static method to prompt to save just returns yes/no.  We need to recreate that
		//dialog to paint it:
		JFrame frame = new JFrame();
		String dialogTitle = "Save Changes";
		JComponent content = QDialog.createContentPanel("Do you want to save the changes you made in this document?", 
						"Your changes will be lost if you don't save them.", 
						null, //innerComponent
						true); //selectable
		
		DialogFooter footer = DialogFooter.createDialogFooter(new JComponent[] {},
				DialogFooter.SAVE_DONT_SAVE_CANCEL_OPTION, EscapeKeyBehavior.TRIGGERS_CANCEL);
		
		final QDialog dialog = new QDialog(frame, dialogTitle, QDialog.getIcon(QDialog.PLAIN_MESSAGE), content, footer, true);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				dialog.addNotify();
				dialog.pack();
			}
		});
		
		Component comp = dialog.getContentPane();
		BufferedImage image = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		comp.paint(g);
		g.dispose();
		frame.setVisible(false);
		return BasicThumbnail.getShadow(3).create(image, preferredSize);
	}
}
