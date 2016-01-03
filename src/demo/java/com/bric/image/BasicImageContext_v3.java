/*
 * @(#)BasicImageContext_v3.java
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
 * <p>This v3 draft inlines calls to <code>PerspectiveTransform.transform(..)</code>.
 * Compared to the previous draft: this
 * reduced the overall computation to 80%.
 * @see com.bric.image.BasicImageContextDemo
 */
public class BasicImageContext_v3 extends ImageContext {
	final int width, height;
    final int[] data;
    final int stride;
	final BufferedImage bi;
	boolean disposed = false;
	
	/** Create a Graphics3D context that paints to a destination image.
	 * 
	 * @param bi an RGB or ARGB image.
	 */
	public BasicImageContext_v3(BufferedImage bi) {
		int type = bi.getType();
		if(!(type==BufferedImage.TYPE_INT_ARGB || type==BufferedImage.TYPE_INT_RGB)) {
			throw new IllegalArgumentException("only TYPE_INT_RGB and TYPE_INT_ARGB are supported");
		}
		this.bi = bi;
		width = bi.getWidth();
		height = bi.getHeight();
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
		int maxXi = Math.min(width, MathG.ceilInt(maxX)+1);
		int minYi = Math.max(0, MathG.floorInt(minY)-1);
		int maxYi = Math.min(height, MathG.ceilInt(maxY)+1);
		
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
		
		Object interpolationHint = getInterpolationRenderingHint();
		
		double transformedX, transformedY;
		double m00, m01, m02, m10, m11, m12, m20, m21, m22, w;
		
		{
			double[][] matrix = new double[3][3];
			pt.getMatrix(matrix);
			m00 = matrix[0][0];
			m01 = matrix[0][1];
			m02 = matrix[0][2];
			m10 = matrix[1][0];
			m11 = matrix[1][1];
			m12 = matrix[1][2];
			m20 = matrix[2][0];
			m21 = matrix[2][1];
			m22 = matrix[2][2];
		}
        
		if(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(interpolationHint)) {
			for(int y = minYi; y<=maxYi; y++) {
				for(int x = minXi; x<=maxXi; x++) {
					if(y>=0 && y<height && x>=0 && x<width) {

						//transform (x,y) to (transformedX, transformedY):
						w = m20 * x + m21 * y + m22;
						transformedX = (m00 * x + m01 * y + m02) / w;
						transformedY = (m10 * x + m11 * y + m12) / w;
						
						int newX = (int)(transformedX+.5);
						int newY = (int)(transformedY+.5);
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
					if(y>=0 && y<height && x>=0 && x<width) {
                        int samples = windowArea;
						int srcA = 0;
						int r = 0;
						int g = 0;
						int b = 0;
						for(double dx = 0; dx<windowLengthD; dx++) {
							for(double dy = 0; dy<windowLengthD; dy++) {
								double x2 = x+dx*incr;
								double y2 = y+dy*incr;
								
								//transform (x,y) to (transformedX, transformedY):
								w = m20 * x2 + m21 * y2 + m22;
								transformedX = (m00 * x2 + m01 * y2 + m02) / w;
								transformedY = (m10 * x2 + m11 * y2 + m12) / w;
								
								int newX = (int)(transformedX-.00001);
								int newY = (int)(transformedY-.00001);
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
