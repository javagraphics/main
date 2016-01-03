/*
 * @(#)BasicImageContext_v11.java
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
 * <p>This v11 draft uses bitwise or instead of add: this does not
 * improve performance.
 * @see com.bric.image.BasicImageContextDemo
 */
public class BasicImageContext_v11 extends ImageContext {
	final int width, height;
    final int[] data;
    final int stride;
	final BufferedImage bi;
	boolean disposed = false;
	
	/** Create a Graphics3D context that paints to a destination image.
	 * 
	 * @param bi an RGB or ARGB image.
	 */
	public BasicImageContext_v11(BufferedImage bi) {
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
		int minXi = MathG.floorInt(minX)-1;
		int maxXi = MathG.ceilInt(maxX)+1;
		int minYi = MathG.floorInt(minY)-1;
		int maxYi = MathG.ceilInt(maxY)+1;
		
		//bound everything from [0,limit)
		minXi = Math.max(0, Math.min(width-1, minXi));
		maxXi = Math.max(0, Math.min(width-1, maxXi));
		minYi = Math.max(0, Math.min(height-1, minYi));
		maxYi = Math.max(0, Math.min(height-1, maxYi));
		
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
        boolean oHasAlpha = img.getColorModel().hasAlpha();
		
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
            if (oHasAlpha) {
				for(int y = minYi; y<=maxYi; y++) {
                    int yw = y * stride;
                    double yd = y;
					for(int x = minXi; x<=maxXi; x++) {
	                    double xd = x;
	
						//transform (x,y) to (transformedX, transformedY):
						w = m20 * xd + m21 * yd + m22;
						transformedX = (m00 * xd + m01 * yd + m02) / w;
						transformedY = (m10 * xd + m11 * yd + m12) / w;
						
						int newX = (int)(transformedX+.5);
						int newY = (int)(transformedY+.5);
						if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
                            int src = otherPixels[newY * os + newX];
                            int srcA = src >>> 24;
							if(srcA==255) {
								data[yw+x] = src;
							} else if(srcA>0) {
								int r = (src >> 16) & 0xff;
								int g = (src >> 8) & 0xff;
								int b = src & 0xff;
                                int dst = data[yw + x];
								int dstA = (dst >> 24) & 0xff;
	                            int dstAX = (dstA) * (255 - srcA);
								int dstR = (dst >> 16) & 0xff;
								int dstG = (dst >> 8) & 0xff;
								int dstB = dst & 0xff;
								int srcAX = srcA * 255;
								int resA = srcAX + dstAX;
								
								if(resA!=0) {
									r = (r*srcAX + dstR * dstAX)/resA;
									g = (g*srcAX + dstG * dstAX)/resA;
									b = (b*srcAX + dstB * dstAX)/resA;
									data[yw+x] = (resA / 255 << 24) | 
											((r>255) ? 0xff0000 : r << 16) |
											((g>255) ? 0xff00 : g << 8) |
											((b>255) ? 0xff : b);
								}
							}
						}
					}
				}
            } else {
				for(int y = minYi; y<=maxYi; y++) {
                    int yw = y * stride;
                    double yd = y;
					for(int x = minXi; x<=maxXi; x++) {
	                    double xd = x;
	
						//transform (x,y) to (transformedX, transformedY):
						w = m20 * xd + m21 * yd + m22;
						transformedX = (m00 * xd + m01 * yd + m02) / w;
						transformedY = (m10 * xd + m11 * yd + m12) / w;
						
						int newX = (int)(transformedX+.5);
						int newY = (int)(transformedY+.5);
						if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
							data[yw+x] = otherPixels[newY*os+newX];
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

            if (oHasAlpha) {
				for(int y = minYi; y<=maxYi; y++) {
                    int yw = y * stride;
                    double yd = y;
					for(int x = minXi; x<=maxXi; x++) {
	                    double xd = x;
                        int samples = windowArea;
						int srcA = 0;
						int r = 0;
						int g = 0;
						int b = 0;
						for(double dx = 0; dx<windowLengthD; dx++) {
							for(double dy = 0; dy<windowLengthD; dy++) {
								double x2 = xd+dx*incr;
								double y2 = yd+dy*incr;
	
								//transform (x,y) to (transformedX, transformedY):
								w = m20 * x2 + m21 * y2 + m22;
								transformedX = (m00 * x2 + m01 * y2 + m02) / w;
								transformedY = (m10 * x2 + m11 * y2 + m12) / w;
								
								int newX = (int)(transformedX-.00001);
								int newY = (int)(transformedY-.00001);
								if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
                                    int opix = otherPixels[newY * os + newX];
    								srcA += (opix >> 24) & 0xff;
									r += opix & 0xff0000;
									g += opix & 0xff00;
									b += opix & 0xff;
								} else {
									samples--;
								}
							}
						}
						if(samples>0) {
							srcA = srcA/samples;
							r = (r >>> 16)/samples;
							g = (g >>> 8)/samples;
							b = b/samples;
							if(srcA==255) {
								data[yw+x] = 0xff000000 | (r << 16) | (g << 8) | b;
							} else if(srcA>0) {
	                            int dst = data[yw+x];
	                            int dstAX = (dst >>> 24) * (255 - srcA);
								int dstR = (dst >> 16) & 0xff;
								int dstG = (dst >> 8) & 0xff;
								int dstB = dst & 0xff;
		                        int srcAX = srcA * 255;
		                        int resA = (srcAX + dstAX);
		                        
		                        if(resA!=0) {
		                            r = (r * srcAX + dstR * dstAX) / resA;
		                            g = (g * srcAX + dstG * dstAX) / resA;
		                            b = (b * srcAX + dstB * dstAX) / resA;
									data[yw+x] = (resA / 255 << 24) | 
											((r>255) ? 0xff0000 : r << 16) |
											((g>255) ? 0xff00 : g << 8) |
											((b>255) ? 0xff : b);
		                        }
							}
						}
					}
				}
			} else {
				for(int y = minYi; y<=maxYi; y++) {
                    int yw = y * stride;
                    double yd = y;
					for(int x = minXi; x<=maxXi; x++) {
	                    double xd = x;

                        int samples = windowArea;
						int srcA = 0;
						int r = 0;
						int g = 0;
						int b = 0;
						for(double dx = 0; dx<windowLengthD; dx++) {
							for(double dy = 0; dy<windowLengthD; dy++) {
								double x2 = xd+dx*incr;
								double y2 = yd+dy*incr;
	
								//transform (x,y) to (transformedX, transformedY):
								w = m20 * x2 + m21 * y2 + m22;
								transformedX = (m00 * x2 + m01 * y2 + m02) / w;
								transformedY = (m10 * x2 + m11 * y2 + m12) / w;
								
								int newX = (int)(transformedX-.00001);
								int newY = (int)(transformedY-.00001);
								if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
                                    int opix = otherPixels[newY * os + newX];
                                    srcA += 255;
									r += opix & 0xff0000;
									g += opix & 0xff00;
									b += opix & 0xff;
								} else {
									samples--;
								}
							}
						}
						if(samples>0) {
							srcA = srcA/samples;
							r = (r >>> 16)/samples;
							g = (g >>> 8)/samples;
							b = b/samples;
							if(srcA==255) {
								data[yw+x] = 0xff000000 | (r << 16) | (g << 8) | b;
							} else if(srcA>0) {
								int dst = data[yw+x];
		                        int dstAX = (dst >>> 24) * (255 - srcA);
								int dstR = (dst >> 16) & 0xff;
								int dstG = (dst >> 8) & 0xff;
								int dstB = dst & 0xff;
		                        int srcAX = srcA * 255;
		                        int resA = (srcAX + dstAX);
		                        
		                        if(resA!=0) {
		                            r = (r * srcAX + dstR * dstAX) / resA;
		                            g = (g * srcAX + dstG * dstAX) / resA;
		                            b = (b * srcAX + dstB * dstAX) / resA;
									data[yw+x] = (resA / 255 << 24) | 
											((r>255) ? 0xff0000 : r << 16) |
											((g>255) ? 0xff00 : g << 8) |
											((b>255) ? 0xff : b);
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
