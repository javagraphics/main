/*
 * @(#)ImageComparison.java
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
package com.bric.image;

import java.awt.image.BufferedImage;

import com.bric.image.pixel.BufferedImageIterator;
import com.bric.image.pixel.IntARGBConverter;
import com.jhlabs.image.GaussianFilter;

/** This contains static methods related to comparing images. */
public class ImageComparison {
	
	/** Check to see if two images are nearly identical.
	 * This creates a blurred copy of both images, and uses a certain
	 * level of error tolerance. With these precautions: this should generally
	 * notice when images are nearly the same.
	 */
	public static boolean equals(BufferedImage bi1,BufferedImage bi2) {
		int w1 = bi1.getWidth();
		int h1 = bi1.getHeight();
		int w2 = bi2.getWidth();
		int h2 = bi2.getHeight();
		int type1 = bi1.getType();
		int type2 = bi2.getType();
		if(w1!=w2 || h1!=h2 || type1!=type2) {
			return false;
		}
		GaussianFilter gf = new GaussianFilter(5);
		BufferedImage copy1 = gf.createCompatibleDestImage(bi1, null);
		BufferedImage copy2 = gf.createCompatibleDestImage(bi2, null);
		gf.filter(bi1, copy1);
		gf.filter(bi2, copy2);
		
		IntARGBConverter iter1 = new IntARGBConverter(BufferedImageIterator.get(copy1));
		IntARGBConverter iter2 = new IntARGBConverter(BufferedImageIterator.get(copy2));
		int[] row1 = new int[w1];
		int[] row2 = new int[w2];
		int tolerance = 10;
		int toleranceSquared = tolerance*tolerance;
		while(!iter1.isDone()) {
			iter1.next(row1);
			iter2.next(row2);
			for(int x = 0; x<w1; x++) {
				int a1 = (row1[x] >> 24) & 0xff;
				int r1 = (row1[x] >> 16) & 0xff;
				int g1 = (row1[x] >> 8) & 0xff;
				int b1 = (row1[x] >> 0) & 0xff;
				int a2 = (row2[x] >> 24) & 0xff;
				int r2 = (row2[x] >> 16) & 0xff;
				int g2 = (row2[x] >> 8) & 0xff;
				int b2 = (row2[x] >> 0) & 0xff;
				int alphaDistance = (a1-a2)*(a1-a2);
				if( alphaDistance > toleranceSquared)
					return false;
				
				if(a1>50 && a2>50) {
					int rgbDistance = (r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2);
					if(rgbDistance > toleranceSquared)
						return false;
				}
				
			}
		}
		return true;
	}
}
