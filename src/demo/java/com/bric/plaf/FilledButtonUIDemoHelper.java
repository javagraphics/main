/*
 * @(#)FilledButtonUIDemoHelper.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ButtonUI;

import com.bric.blog.BlogHelper;

public class FilledButtonUIDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredMaxSize) {
		JButton button = new JButton("RoundRectButtonUI");
		button.setUI(new RoundRectButtonUI());
		button.setSize(button.getPreferredSize());
		BufferedImage image = new BufferedImage(button.getWidth(), button.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		button.paint(g);
		g.dispose();
		return image;
	}
	
	/** Creates a directory named "filledbuttonui" in the directory provided.
	 * 
	 * @param directory the master directory to contain all resources/subdirectories
	 * @return the directory titled "filledbuttonui" that is populated with other files.
	 * @throws Exception if an error occurred creating these demo files.
	 */
	public static File createScreenshots(File directory) throws Exception {
		final File dir = new File(directory, "filledbuttonui");
		if((!dir.exists()) && (!dir.mkdirs()))
			throw new RuntimeException("mkdirs failed for "+dir.getAbsolutePath());
		
		Class<?>[] types = getNonAbstractSubclasses(FilledButtonUI.class, "com.bric");
		for(Class<?> type : types) {
			final ButtonUI ui = (ButtonUI) type.newInstance();
			final BufferedImage[] dest = new BufferedImage[] { null };
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					JButton button = new JButton("Sample");
					button.setUI(ui);
					button.setSize(button.getPreferredSize());
					dest[0] = paint(button, 4);
				}
			});
			String name = type.getName();
			name = name.substring(name.lastIndexOf('.')+1);
			File destFile = new File(dir, name+".png");
			ImageIO.write(dest[0], "png", destFile);
		}
		return dir;
	}
	
	/** Paint a JComponent as a BufferedImage
	 * 
	 * @param jc the component to paint
	 * @param padding a uniform padding
	 * @return an image rendering of this component
	 */
	protected static BufferedImage paint(JComponent jc,int padding) {
		Dimension d = jc.getSize();
		BufferedImage bi = new BufferedImage(d.width + 2*padding, d.height+2*padding, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,bi.getWidth(),bi.getHeight());
		g.translate(padding, padding);
		jc.paint(g);
		g.dispose();
		return bi;
	}
}
