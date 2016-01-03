/*
 * @(#)ImageReader.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
package com.bric.viewer.reader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bric.image.ImageSize;
import com.bric.image.pixel.Scaling;
import com.bric.io.location.IOLocation;

public class ImageReader extends FileReader {
	
	class ImageViewer extends JPanel {
		private static final long serialVersionUID = 1L;

		BufferedImage bi = null;
		ImageViewer(BufferedImage bi) {
			if(bi==null) throw new NullPointerException();
			
			this.bi = bi;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			//TODO: scale to fit
			g.drawImage(bi, getWidth()/2 - bi.getWidth()/2, getHeight()/2 - bi.getHeight()/2, null);
		}
	}
	
	public String getDescription() {
		return "Images";
	}

	@Override
	public String[] getExtensions() {
		return new String[] { "jpg", "jpeg", "png", "gif", "bmp" };
	}

	@Override
	public JComponent getComponent(IOLocation loc,Dimension expectedSize) throws IOException {
		URL url = getURL(loc);
		BufferedImage image = null;
		if(expectedSize!=null) {
			Dimension imageSize = ImageSize.get(url);
			if(imageSize.width>expectedSize.height || imageSize.height>expectedSize.height) {
				Dimension scaledSize = Scaling.scaleDimensionsProportionally(imageSize, expectedSize);
				image = Scaling.scale(url, BufferedImage.TYPE_INT_ARGB, scaledSize);
			}
		}
		if(image==null) {
			image = Scaling.scale(url, BufferedImage.TYPE_INT_ARGB, null);
		}
		
		return new ImageViewer(image);
	}

	
}
