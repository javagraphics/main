/*
 * @(#)PrintLayoutDialogDemo.java
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
package com.bric.print.swing;

import java.awt.print.PageFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.awt.DemoPaintable;
import com.bric.awt.Paintable;
import com.bric.blog.Blurb;
import com.bric.print.PrintLayout;

@Blurb (
filename = "PrintLayoutDialog",
title = "Print Dialogs: Laying Out Tiles",
releaseDate = "November 2008",
summary = "This introduces <a href=\"https://javagraphics.java.net/doc/com/bric/print/PrintLayout.html\">an object</a> " +
"to lay out several graphic objects on a sheet of paper, and a dialog to preview and edit this layout.\n" +
"<p>(Oh: and it prints.)",
link = "http://javagraphics.blogspot.com/2008/11/print-dialogs-laying-out-tiles.html",
sandboxDemo = true
)
public class PrintLayoutDialogDemo {


	/** A simple demo showing several possible variations of this dialog,
	 * and demonstrating the effects of Cancel/OK/Print
     * @param args the application's arguments. (This is unused.)
	 */
	public static void main(String[] args) {
		System.out.println("Running PrintLayoutDialog in Java "+System.getProperty("java.version")+
				" on "+System.getProperty("os.name")+" "+System.getProperty("os.version"));
		JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		PrintLayout printLayout = new PrintLayout(2,2,new PageFormat());

		
		printLayout.setHeader("<table width=\"100%\"><tr><td align=\"center\">"+(new Date()).toString()+"</td></tr></table>");
		printLayout.setFooter("<table width=\"100%\"><tr><td align=\"left\">http://javagraphics.blogspot.com/</td>\n"+
		"<td align=\"right\">Page <PageNumber/> of <PageCount/></td></tr></table>");
		
		Paintable[] paintables = new Paintable[200];
		for(int a = 0; a<paintables.length; a++) {
			paintables[a] = new DemoPaintable(640, 480, Integer.toString(a+1));
		}
		final PrintLayoutDialog dialog = new PrintLayoutDialog(f,"Print Layout",printLayout,paintables,"http://postsecret.blogspot.com/");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);

				dialog.getPropertiesPanel().layoutControls(true,true);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);

				dialog.getPreviewPanel().setVisible(false);
				dialog.getPropertiesPanel().layoutControls(false,true);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);


				dialog.getPropertiesPanel().layoutControls(false,false);
				dialog.pack();
				dialog.setLocationRelativeTo(null);
				dialog.setVisible(true);
				
				System.exit(0);
			}
		});
	}
}
