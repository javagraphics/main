package com.bric.text;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class JavaTextComponentHighlighterDemoHelper {

	public static BufferedImage createBlurbGraphic(final Dimension preferredSize) throws Exception {
		final BufferedImage[] imagePtr = new BufferedImage[] { null };
		final JavaTextComponentHighlighterDemo[] demoPtr = new JavaTextComponentHighlighterDemo[] { null };
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				demoPtr[0] = new JavaTextComponentHighlighterDemo(false);
				demoPtr[0].scrollPane.getHorizontalScrollBar().setVisible(false);
				demoPtr[0].scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				demoPtr[0].scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
				if(preferredSize==null) {
					demoPtr[0].pack();
				} else {
					demoPtr[0].setSize(preferredSize);
				}
				demoPtr[0].textPane.setText("if( c=='e' ) {\n  return Math.E;\n}\n");
				demoPtr[0].highlighter.refresh(false);
			}
		});
		
		try {
			Thread.sleep(1000);
		} catch(Exception e) {}
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				demoPtr[0].setVisible(true);
				Dimension d = demoPtr[0].getSize();
				imagePtr[0] = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = imagePtr[0].createGraphics();
				demoPtr[0].paint(g);
				g.dispose();
				demoPtr[0].setVisible(false);
			}
		});
		
		return imagePtr[0];
	}
}
