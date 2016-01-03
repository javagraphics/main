/*
 * @(#)AbstractImageContextDemo.java
 *
 * $Date: 2014-05-04 17:57:20 +0200 (So, 04 Mai 2014) $
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
package com.bric.image;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.bric.image.transition.AbstractTransition;
import com.bric.swing.TableDataApp;

/** An abstract superclass for demo apps that want to compare ImageContexts. */
public abstract class AbstractImageContextDemo extends TableDataApp {

	private static final long serialVersionUID = 1L;

	public AbstractImageContextDemo(String labelText, Object[] columnNames) {
		super(labelText, columnNames, 6);
	}

	BufferedImage sampleImage = AbstractTransition.createImage(1000, "YZ", true, true);
	
	Object[] hints = new Object[] {
			RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR,
			RenderingHints.VALUE_INTERPOLATION_BICUBIC
	};
	Boolean[] alphas = new Boolean[] { false, true };
	
	protected void clearImage(BufferedImage bi) {
		Graphics2D g = bi.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0,0,bi.getWidth(),bi.getHeight());
		g.dispose();
	}
	
	/** Create a translucent copy of the argument image.
	 * 
	 * @param alpha the alpha amount to multiply each pixel by.
	 * */
	protected BufferedImage createTranslucentCopy(BufferedImage bi,float alpha) {
		BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = copy.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(bi,0,0,null);
		g.dispose();
		return copy;
	}
	
	protected AffineTransform getImageTransform(int imageIndex) {
		Random random = new Random(imageIndex);
		AffineTransform tx = new AffineTransform();
		int w = sampleImage.getWidth();
		int h = sampleImage.getHeight();
		tx.translate(w/2, h/2);
		double scale = random.nextDouble()*.5+.5;
		tx.scale(scale, scale);
		tx.rotate(random.nextDouble());
		tx.translate(-w/2, -h/2);
		return tx;
	}

	@Override
	protected Object createTableValue(int row, int col) {
		int hintIndex = row%3;
		int alphaIndex = row/3;
		
		//this indicates we're not done constructing our fields yet:
		while(hints==null || alphas==null) {
			try {
				Thread.sleep(50);
			} catch(Exception e) {}
		}

		String name;
		if( RenderingHints.VALUE_INTERPOLATION_BICUBIC.equals(hints[hintIndex]) ) {
			name = "Bicubic";
		} else if( RenderingHints.VALUE_INTERPOLATION_BILINEAR.equals(hints[hintIndex]) ) {
			name = "Bilinear";
		} else if( RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(hints[hintIndex]) ) {
			name = "Nearest Neighbor";
		} else {
			name = "Unknown";
		}
		if(alphas[alphaIndex]) {
			name = name+", alpha";
		}
		
		if(col==0) {
			return name;
		}

		return createTableValue(col, hints[hintIndex], alphas[alphaIndex], name);
	}

	protected abstract Object createTableValue(int col,Object interpolationHint,boolean useAlpha,String name);
		
}
