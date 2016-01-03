/*
 * @(#)ParallelImageContext.java
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashSet;
import java.util.Set;

import javax.media.jai.PerspectiveTransform;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import com.bric.math.MathG;

/** This is an unfinished/untested <code>ImageContext</code> that
 * was supposed to use the Aparapi architecture to use GPU's to
 * process the image transform: but due to hardware limitations
 * I'm unable to further study this at this time.
 *
 */
public class ParallelImageContext extends ImageContext {

	final int width, height;
	final int[] data;
	final int stride;
	final BufferedImage bi;
	boolean disposed = false;

	/** Create a Graphics3D context that paints to a destination image.
	 * 
	 * @param bi an RGB or ARGB image.
	 */
	public ParallelImageContext(BufferedImage bi) {
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
		final int minXi = Math.max(0, Math.min(width-1, MathG.floorInt(minX)-1));
		final int maxXi = Math.max(0, Math.min(width-1, MathG.ceilInt(maxX)+1));
		final int minYi = Math.max(0, Math.min(height-1, MathG.floorInt(minY)-1));
		final int maxYi = Math.max(0, Math.min(height-1, MathG.ceilInt(maxY)+1));

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

		final int[] otherPixels = getPixels(img);
		final int os = img.getRaster().getWidth();
		final int ow = img.getWidth();
		final int oh = img.getHeight();
		final boolean oHasAlpha = img.getColorModel().hasAlpha();

		final Object interpolationHint = getInterpolationRenderingHint();

		final double m00, m01, m02, m10, m11, m12, m20, m21, m22;

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


		for(int y = minYi; y<=maxYi; y++) {
			Kernel kernel = null;
			final int yw = y * stride;
			final double yd = y;
			try {
				//create the appropriate kernel:
				if(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(interpolationHint)) {
					if (oHasAlpha) {
						kernel = new Kernel(){
							@Override 
							public void run() {
								int i = getGlobalId() + minXi + yw;
								int x = i%stride;
								double xd = x;

								double w = m20 * xd + m21 * yd + m22;
								double transformedX = (m00 * xd + m01 * yd + m02) / w;
								double transformedY = (m10 * xd + m11 * yd + m12) / w;

								int newX = (int)(transformedX+.5);
								int newY = (int)(transformedY+.5);
								if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
									int src = otherPixels[newY * os + newX];
									int srcA = src >>> 24;
									if(srcA==255) {
										data[i] = src;
									} else if(srcA>0) {
										int r = (src >> 16) & 0xff;
										int g = (src >> 8) & 0xff;
										int b = src & 0xff;
										int dst = data[i];
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
											data[i] = (resA / 255 << 24) | 
													((r>255) ? 0xff0000 : r << 16) |
													((g>255) ? 0xff00 : g << 8) |
													((b>255) ? 0xff : b);
										}
									}
								}
							}
						};
					} else {
						kernel = new Kernel(){
							@Override 
							public void run() {
								int i = getGlobalId() + minXi + yw;
								int x = i%stride;
								double xd = x;

								double w = m20 * xd + m21 * yd + m22;
								double transformedX = (m00 * xd + m01 * yd + m02) / w;
								double transformedY = (m10 * xd + m11 * yd + m12) / w;

								int newX = (int)(transformedX+.5);
								int newY = (int)(transformedY+.5);
								if(newY>=0 && newY<oh && newX>=0 && newX<ow) {
									data[i] = otherPixels[newY*os+newX];
								}
							}
						};
					}
				} else {
					final int windowLength;
					if(RenderingHints.VALUE_INTERPOLATION_BICUBIC.equals(interpolationHint)) {
						windowLength = 4;
					} else {
						windowLength = 2;
					}
					final double windowLengthD = windowLength;
					final double incr = 1.0 / (windowLengthD-1.0);
					final int windowArea = windowLength*windowLength;

					if (oHasAlpha) {
						kernel = new Kernel(){
							@Override 
							public void run() {
								int i = getGlobalId() + minXi + yw;
								int x = i%stride;
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
										double w = m20 * x2 + m21 * y2 + m22;
										double transformedX = (m00 * x2 + m01 * y2 + m02) / w;
										double transformedY = (m10 * x2 + m11 * y2 + m12) / w;

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
										data[i] = 0xff000000 | (r << 16) | (g << 8) | b;
									} else if(srcA>0) {
										int dst = data[i];
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
											data[i] = (resA / 255 << 24) | 
													((r>255) ? 0xff0000 : r << 16) |
													((g>255) ? 0xff00 : g << 8) |
													((b>255) ? 0xff : b);
										}
									}
								}
							}
						};
					} else {
						kernel = new Kernel(){
							@Override 
							public void run() {
								int i = getGlobalId() + minXi + yw;
								int x = i%stride;
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

										double w = m20 * x2 + m21 * y2 + m22;
										double transformedX = (m00 * x2 + m01 * y2 + m02) / w;
										double transformedY = (m10 * x2 + m11 * y2 + m12) / w;

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
										data[i] = 0xff000000 | (r << 16) | (g << 8) | b;
									} else if(srcA>0) {
										int dst = data[i];
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
											data[i] = (resA / 255 << 24) | 
													((r>255) ? 0xff0000 : r << 16) |
													((g>255) ? 0xff00 : g << 8) |
													((b>255) ? 0xff : b);
										}
									}
								}
							}
						};
					}
				}

				Range range = Range.create(maxXi-minXi);
				printOnce(kernel.getExecutionMode().toString());
				kernel.execute(range);

			} finally {
				if(kernel!=null) {
					kernel.dispose();
				}
			}
		}
	}

	static Set<String> printedStrings = new HashSet<String>();
	protected static synchronized void printOnce(String s) {
		if(printedStrings.add(s))
			System.out.println(s);
	}

	/** Commit all changes back to the BufferedImage this context paints to.
	 */
	public synchronized void dispose() {
		disposed = true;
	}
}
