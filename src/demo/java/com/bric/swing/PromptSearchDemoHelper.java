/*
 * @(#)PromptSearchDemoHelper.java
 *
 * $Date: 2014-06-04 22:21:33 +0200 (Mi, 04 Jun 2014) $
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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JTextField;

import com.bric.blog.BlogHelper;
import com.bric.plaf.RoundTextFieldUI;

public class PromptSearchDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		JTextField f1 = new JTextField();
		JTextField f2 = new JTextField();
		f2.putClientProperty("useSearchIcon", Boolean.TRUE);
		f2.setUI(new RoundTextFieldUI());
		new TextFieldPrompt(f1, "Prompt");
		new TextFieldPrompt(f2, "Search");

		f1.validate();
		f2.validate();
		Dimension d1 = f1.getPreferredSize();
		Dimension d2 = f2.getPreferredSize();
		d1.width = preferredSize.width-20;
		d2.width = preferredSize.width-20;
		f1.setSize(d1);
		f2.setSize(d2);
		
		//let the EDT catch up and run some invocations:
		try {
			Thread.sleep(1000);
		} catch(Exception e) {}
		
		BufferedImage bi = new BufferedImage( Math.max(d1.width, d2.width), d1.height + d2.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		f1.paint(g);
		g.translate(0, d1.height);
		f2.paint(g);
		g.dispose();
		return bi;
	}
}
