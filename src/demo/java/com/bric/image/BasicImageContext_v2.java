/*
 * @(#)BasicImageContext_v2.java
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
package com.bric.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.media.jai.PerspectiveTransform;

import com.bric.math.MathG;

/** This is a simple Java implementation of image transformations.
 * <p>This v2 draft changes the two-dimensional array used to represent
 * pixels to a one-dimensional array. Compared to the previous draft: this
 * reduced the overall computation to 88%.
 * @see com.bric.image.BasicImageContextDemo
 */
public class BasicImageContext_v2 extends ImageContext {
	final int w, h;
    final int[] data;
    final int stride;
	final BufferedImage bi;
	boolean disposed = false;
	
	/** Create a Graphics3D context that paints to a destination image.
	 * 
	 * @param bi an RGB or ARGB image.
	 */
	public BasicImageContext_v2(BufferedImage bi) {
		int type = bi.getType();
		if(!(type==BufferedImage.TYPE_INT_ARGB || type==BufferedImage.TYPE_INT_RGB)) {
			throw new IllegalArgumentException("only TYPE_INT_RGB and TYPE_INT_ARGB are supported");
		}
		this.bi = bi;
		w = bi.getWidth();
		h = bi.getHeight();
        stride = bi.getRaster().getWidth();
		data = getPixels(bi);
	}
	
	/** Return all the pixels in the argument in ARGB format. */
	protected int[] getPixels(BufferedImage bi) {
        if ((bi.getType() != BufferedImage.TYPE_INT_ARGB && bi.getType() != BufferedImage.TYPE_INT_RGB)
                || !(bi.getRaster().getDataBuffer() instanceof DataBufferInt)) {
            BufferedImage tmp = bi;
            bi = new BufferedImage(tmp.getWidth(), tmp.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bi.createGraphics();
            g.drawImage(tmp, 0, 0, null);
            g.dispose();
        }

        DataBufferInt buf = (DataBufferInt) bi.getRaster().getDataBuffer();
        int[] p = buf.getData();
        return p;
	}

	/** Draw an image to this Graphics3D.
	 * <p>This respects the interpolation rendering hints. When the
	 * interpolation hint is missing, this will also consult the antialiasing
	 * hint or the render hint. The bilinear hint is used by default.
	 * <p>This uses a source over composite.
	 * 
	 * @param img the image to draw.
	 * @param topLeft where the top-left corner of this image will be painted.
	 * @param topRight where the top-right corner of this image will be painted.
	 * @param bottomRight where the bottom-right corner of this image will be painted.
	 * @param bottomLeft where the bottom-left corner of this image will be painted.
	 */
	public synchronized void drawImage(BufferedImage img,Point2D topLeft,Point2D topRight,Point2D bottomRight,Point2D bottomLeft) {
		if(disposed)
			throw new IllegalStateException("This Graphics3D context has been disposed.");
		Point2D srcTopLeft = new Point2D.Double(0,0);
		Point2D srcTopRight = new Point2D.Double(img.getWidth(),0);
		Point2D srcBottomLeft = new Point2D.Double(0,img.getHeight());
		Point2D srcBottomRight = new Point2D.Double(img.getWidth(),img.getHeight());
		
		double minX = Math.min( Math.min(topLeft.getX(), topRight.getX()), 
				Math.min(bottomLeft.getX(), bottomRight.getX()) );
		double maxX = Math.max( Math.max(topLeft.getX(), topRight.getX()), 
				Math.max(bottomLeft.getX(), bottomRight.getX()) );
		double minY = Math.min( Math.min(topLeft.getY(), topRight.getY()), 
				Math.min(bottomLeft.getY(), bottomRight.getY()) );
		double maxY = Math.max( Math.max(topLeft.getY(), topRight.getY()), 
				Math.max(bottomLeft.getY(), bottomRight.getY()) );
		int minXi = Math.max(0, MathG.floorInt(minX)-1);
		int maxXi = Math.min(w, MathG.ceilInt(maxX)+1);
		int minYi = Math.max(0, MathG.floorInt(minY)-1);
		int maxYi = Math.min(h, MathG.ceilInt(maxY)+1);
		
		PerspectiveTransform pt = PerspectiveTransform.getQuadToQuad(
				topLeft.getX(), topLeft.getY(),
				topRight.getX(), topRight.getY(),
				bottomLeft.getX(), bottomLeft.getY(),
				bottomRight.getX(), bottomRight.getY(),
				srcTopLeft.getX(), srcTopLeft.getY(),
				srcTopRight.getX(), srcTopRight.getY(),
				srcBottomLeft.getX(), srcBottomLeft.getY(),
				srcBottomRight.getX(), srcBottomRight.getY()
		);
		
		int[] otherPixels = getPixels(img);
        int os = img.getRaster().getWidth();
        int ow = img.getWidth();
        int oh = img.getHeight();
		
        Point2D p = new Point2D.Double();
		
		Object interpolationHint = getInterpolationRenderingHint();
		
		if(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(interpolationHint)) {
			for(int y = minYi; y<=maxYi; y++) {
				for(int x = minXi; x<=maxXi; x++) {
					if(y>=0 && y<h && x>=0 && x<w) {
						p.setLocation(x, y);
						pt.transform(p, p);
						int newX = (int)(p.getX()+.5);
						int newY = (int)(p.getY()+.5);
						if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
							int srcA = (otherPixels[newY*os+newX] >> 24) & 0xff;
							if(srcA==255) {
								data[y*stride+x] = otherPixels[newY*os+newX];
							} else if(srcA>0) {
								int r = (otherPixels[newY*os+newX] >> 16) & 0xff;
								int g = (otherPixels[newY*os+newX] >> 8) & 0xff;
								int b = (otherPixels[newY*os+newX]) & 0xff;
								int dstA = (data[y*stride+x] >> 24) & 0xff;
                                int dstAX = (dstA) * (255 - srcA);
								int dstR = (data[y*stride+x] >> 16) & 0xff;
								int dstG = (data[y*stride+x] >> 8) & 0xff;
								int dstB = (data[y*stride+x]) & 0xff;
								int srcAX = srcA * 255;
								int resA = srcAX + dstAX;

								if(resA!=0) {
									r = Math.min(255, (r*srcAX + dstR * dstAX)/resA );
									g = Math.min(255, (g*srcAX + dstG * dstAX)/resA );
									b = Math.min(255, (b*srcAX + dstB * dstAX)/resA );
									data[y*stride+x] = (resA / 255 << 24) + (r << 16) + (g << 8) + b;
								}
							}
						}
					}
				}
			}
		} else {
			int windowLength;
			if(RenderingHints.VALUE_INTERPOLATION_BICUBIC.equals(interpolationHint)) {
				windowLength = 4;
			} else {
				windowLength = 2;
			}
			double windowLengthD = windowLength;
			double incr = 1.0 / (windowLengthD-1.0);
			int windowArea = windowLength*windowLength;
			
			for(int y = minYi; y<=maxYi; y++) {
				for(int x = minXi; x<=maxXi; x++) {
					if(y>=0 && y<h && x>=0 && x<w) {
                        int samples = windowArea;
						int srcA = 0;
						int r = 0;
						int g = 0;
						int b = 0;
						for(double dx = 0; dx<windowLengthD; dx++) {
							for(double dy = 0; dy<windowLengthD; dy++) {
								p.setLocation(x+dx*incr, y+dy*incr);
								pt.transform(p, p);
								int newX = (int)(p.getX()-.00001);
								int newY = (int)(p.getY()-.00001);
								if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
									srcA += (otherPixels[newY*os+newX] >> 24) & 0xff;
									r += (otherPixels[newY*os+newX] >> 16) & 0xff;
									g += (otherPixels[newY*os+newX] >> 8) & 0xff;
									b += (otherPixels[newY*os+newX]) & 0xff;
								} else {
									samples--;
								}
							}
						}
						if(samples>0) {
							srcA = srcA/samples;
							r = r/samples;
							g = g/samples;
							b = b/samples;
							if(srcA==255) {
								data[y*stride+x] = 0xff000000 + (r << 16) + (g << 8) + b;
							} else if(srcA>0) {
	                            int dstAX = (data[y*stride+x] >>> 24) * (255 - srcA);
								int dstR = (data[y*stride+x] >> 16) & 0xff;
								int dstG = (data[y*stride+x] >> 8) & 0xff;
								int dstB = (data[y*stride+x]) & 0xff;
	                            int srcAX = srcA * 255;
	                            int resA = (srcAX + dstAX);
	                            
	                            if(resA!=0) {
	                                r = Math.min(255, (r * srcAX + dstR * dstAX) / resA);
	                                g = Math.min(255, (g * srcAX + dstG * dstAX) / resA);
	                                b = Math.min(255, (b * srcAX + dstB * dstAX) / resA);
	                                data[y*stride+x] = (resA / 255 << 24) + (r << 16) + (g << 8) + b;
	                            }
							}
						}
					}
				}
			}
		}
	}
	
	/** Commit all changes back to the BufferedImage this context paints to.
	 */
	public synchronized void dispose() {
		disposed = true;
	}
}
