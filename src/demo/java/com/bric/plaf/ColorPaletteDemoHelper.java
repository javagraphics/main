/*
 * @(#)ColorPaletteDemoHelper.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
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
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import com.bric.blog.BlogHelper;
import com.bric.image.thumbnail.BasicThumbnail;
import com.bric.io.IOUtils;
import com.bric.swing.ColorPalette;

public class ColorPaletteDemoHelper extends BlogHelper {

	
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		ColorPalette palette = new ColorPalette();
		palette.setColor(new Color(0x4F63C3));
		palette.setBackground(Color.white);
		ColorPaletteUI ui = new HSLColorPaletteUI( ColorPaletteDemo.hues, 10, 7*2);
		palette.setUI(ui);
		palette.setSize(palette.getPreferredSize());
		//we have to put it in a frame for the scrollbars to really validate
		JFrame frame = new JFrame();
		frame.getContentPane().add(palette);
		frame.pack();
		BufferedImage image = new BufferedImage(palette.getWidth(), palette.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		palette.paint(g);
		g.dispose();
		return image;
	}
	
	public static File createScreenshots(File dir) throws IOException {
		dir = new File(dir, "ColorPaletteDemo");
		dir.mkdirs();
		
		ColorPaletteDemo cpd = new ColorPaletteDemo();
		for(int a = 0; a<cpd.list.getModel().getSize(); a++) {
			JInternalFrame f = cpd.list.getModel().getElementAt(a);
			BufferedImage bi = paint(f, null);
			File file = IOUtils.getUniqueFile(dir, "sample.png", false, false);
			ImageIO.write(bi, "png", file);
		}
		
		String[] paletteStyles = new String[] {
				ColorSet.PALETTE_STYLE_DEFAULT,
				ColorSet.PALETTE_STYLE_GRADIENT,
				ColorSet.PALETTE_STYLE_STREAKS
		};
		String[] cellStyles = new String[] {
				ColorSet.PALETTE_CELL_STYLE_DEFAULT,
				ColorSet.PALETTE_CELL_STYLE_SCRIBBLE,
				ColorSet.PALETTE_CELL_STYLE_SHADOW
		};
		for(String paletteStyle : paletteStyles) {
			for(String cellStyle : cellStyles) {
				BufferedImage bi = new BufferedImage(160,120, BufferedImage.TYPE_INT_RGB);
				ModifierColorPaletteUI.SHEET_MUTED_GRID.paint(bi, paletteStyle, cellStyle);
				bi = BasicThumbnail.Aqua.create(bi, null);
				File file = IOUtils.getUniqueFile(dir, "style_"+paletteStyle+"_"+cellStyle+".png", false, false);
				ImageIO.write(bi, "png", file);
			}
		}
		
		return dir;
	}
	
}
