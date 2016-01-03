/*
 * @(#)QTJImage.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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
package com.bric.qt;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import quicktime.QTException;
import quicktime.qd.Pict;
import quicktime.qd.PixMap;
import quicktime.qd.QDConstants;
import quicktime.qd.QDGraphics;
import quicktime.qd.QDRect;

/** This maps a <code>QDGraphics</code> to a <code>BufferedImage</code>.
 * <P>This class is designed to accomodate several consecutive calls to transfer
 * data from QT-space to Java-space.  You can reuse the same BufferedImage as follows:
 * <P><code>BufferedImage bi = null;</code>
 * <BR><code>while( (condition) ) {</code>
 * <BR><code>&nbsp; &nbsp; bi = qtji.getImage(bi);</code>
 * <BR><code>&nbsp; &nbsp; (process bi)</code>
 * <BR><code>}</code>
 * <P>This object basically binds itself to a <code>QDGraphics</code> object, and every
 * call to <code>getImage()</code> copies the contents of that <code>QDGraphics</code> into
 * a <code>BufferedImage</code>.
 * <P>Currently this class only works with <code>BufferedImages</code> of type
 * <code>TYPE_INT_ARGB</code>.  This can be adapted in the future, but it fits the original
 * needs for which this class was designed.
 * <P>I'm not entirely sure how this compares with the latest <code>QTImageProducer</code>
 * in the QTJava package regarding performance.  It would be interesting to find out.
 * <P>To the best of my knowledge, there is no efficient way to map a <code>BufferedImage</code>
 * into a <code>QDGraphics</code>, <code>Pict</code> or <code>PixMap</code>.  (That is, once you have
 * an image in Java-space, I don't know of an efficient way to take it into QT-space).
 * <P>This does not open or close a QTSession, it is assumed that you will handle this externally.
 * 
 * @see <a href="http://javagraphics.blogspot.com/2007/04/qtj-from-qdgraphics-to-bufferedimage.html">QTJ: from QDGraphics to BufferedImages</a>
 * @deprecated QuickTime for java is deprecated. (Heck: now even QuickTime is deprecated).
 */
@Deprecated
public class QTJImage {

	/** This is a simple static conversion from a <code>Pict</code> to a <code>BufferedImage</code>.
	 * This should not be used for repeated conversions if you can help it; it is much better
	 * to reuse your <code>BufferedImage</code> and refer to one <code>QTJImage</code> object to avoid
	 * lots of memory allocation.
	 * @param pict the QuickTime image you want to convert
	 * @return a <code>BufferedImage</code>
	 * 
	 * @throws QTException if a QT-related error occurs.
	 */
	public static BufferedImage getImageFromPict(Pict pict) throws QTException {
		QTJImage i = new QTJImage(pict);
		try {
			return i.getImage(null);
		} catch(Error e) {
			//an OutOfMemoryError may occur if the image is too big
			//Intel Macs had a bug that would make the dimensions read in as inaccurately
			//large, but I think that's been fixed recently...
			System.err.println("frame: "+pict.getPictFrame());
			throw e;
		}
	}
	
	/** If <code>isDisposing()</code> returns <code>true</code>, then 
	 * this releases QT resources used to extract the image data.
	 * <P>This method is made public so you can forcefully release
	 * resources.
	 * <P>This should be considered similar to calling <code>InputStream.close()</code>:
	 * once this method is called if you try to use this object <code>Exceptions</code>
	 * will be thrown.
	 */
	@Override
	public void finalize() throws Throwable {
		if(disposing) {
			try {
				g.disposeQTObject();
			} catch(QTException e) {
				/** We've observed that on really screwed up QTJ installs
				 * QTException.printStackTrace throws its own exception.
				 */
				try {
					e.printStackTrace();
				} catch(Throwable t) {
					System.err.println("A serious error occurred while trying to dispose of an object.");
				}
			} finally {
				g = null;
			}
			try {
				pixMap.disposeQTObject();
			} catch(QTException e) {
				try {
					e.printStackTrace();
				} catch(Throwable t) {
					System.err.println("A serious error occurred while trying to dispose of an object.");
				}
			} finally {
				pixMap = null;
			}
		}
		super.finalize();
	}
	
	private QDRect rect;
	private Dimension d;
	private QDGraphics g;
	private PixMap pixMap;
	/** The number of bytes per pixel */
	private int bpp;
	/** The number of bytes per row */
	private int rl;
	private int pixelFormat;
	private byte[] qtData;
	boolean flipHorizontal = false;
	boolean flipVertical = false;
	private boolean disposing = true;
	int[] newArray;

	/** Creates a <code>QTJImage</code> based on a <code>Pict</code>.
	 * <P>This actually boils down creating an empty <code>QTJImage</code>
	 * with the dimensions of the image provided, and then calling
	 * <code>drawPict(pict)</code>.
	 * <P>If you use this constructor, then <code>isDisposingQTObjects()</code>
	 * will return <code>true</code> by default.
	 * 
	 * @param pict the p
	 * @throws QTException
	 */
	public QTJImage(Pict pict) throws QTException {
		this(new QDGraphics(QDConstants.k32ARGBPixelFormat,pict.getPictFrame()));
		drawPict(pict);
		disposing = true;
	}

	/** Creates a <code>QTJImage</code> from the dimensions provided.
	 * @throws QTException if a QT-related error occurs.
	 */
	public QTJImage(Dimension size) throws QTException {
		if(size.width<=0 || size.height<=0) throw new RuntimeException("The dimensions of a QTJImage must be positive integers.");
		QDRect r = new QDRect(0,0,size.width,size.height);
		g = new QDGraphics(QDConstants.k32ARGBPixelFormat,r);
		initialize(g);
	}
	
	/** This creates a <code>QTJImage</code> that is bound to <code>g</code>.
	 * Any subsequent call to <code>QTJImage.getImage()</code> is going to reflect
	 * <code>g</code>, so if you modify <code>g</code> externally the next call to
	 * <code>getImage()</code> should reflect the current state of <code>g</code>.
	 * @param g the <code>QDGraphics</code> to bind this <code>QTJImage</code> to.
	 * <P>WARNING: this object will keep a pointer to <code>g</code>, and unless
	 * you explicitly call <code>setDisposing(false)</code> then <code>g.disposeQTObject()</code>
	 * will be called when this object is finalized.  This is designed to help
	 * minimize the worrying you have to show over memory allocation, but if you
	 * use this constructor and intend to use <code>g</code> beyond the scope of
	 * this object, you should call <code>setDisposing(false)</code>.
	 */
	public QTJImage(QDGraphics g) {
		initialize(g);
	}
	
	/** Initializes some vital info */
	private void initialize(QDGraphics t) {
		rect = t.getBounds();
		d = new Dimension(rect.getWidth(),rect.getHeight());
		newArray = new int[d.width];
		g = t;
		pixMap = g.getPixMap();
		pixelFormat = pixMap.getPixelFormat();
		bpp = pixMap.getPixelSize()/8;
		rl = pixMap.getRowBytes();
	}
	
	/** If this property is <code>true</code>, then when <code>finalize</code>
	 * is called on this method the underlying <code>QDGraphics</code> and
	 * <code>PixMap</code> will have their <code>disposeQTObject</code> methods
	 * invoked.
	 * <P>By default this returns <code>true</code>.
	 * <P>To put this more simply: by default this object will try to clean up
	 * any QT memory allocations.  If you do NOT want this to happen (for example,
	 * if you are using the <code>QDGraphics</code> for something else after this
	 * object falls into garbage collection), then you can call
	 * <code>setDisposing(false)</code> to <i>not</i> let this class try to clean up after itself.
	 */
	public boolean isDisposing() {
		return disposing;
	}
	
	/** This controls whether the this object will try to release the
	 * <code>QDGraphics</code> and <code>PixMap</code>
	 * when this object is garbage collected. 
	 * @see #isDisposing()
	 * 
	 * @param b whether this object will worry about cleaning up the
	 * <code>QTObjects</code>.  By default this property is active.
	 */
	public void setDisposing(boolean b) {
		disposing = b;
	}
	
	/** This draws <code>Pict</code> in this image.
	 * <P>This is one of 2 ways you can modify what this <code>QTJImage</code>
	 * represents visually.  The other way is to call <code>getQDGraphics()</code>
	 * and interact directly with that object.
	 * @throws QTException if a QT-related error occurs.
	 */
	public void drawPict(Pict pict) throws QTException {
		pict.draw(g,rect);
	}
	
	/** This returns the underlying <code>QDGraphics</code> this image
	 * refers to.  With this you can directly manipulate the QT image.
	 * 
	 * <P>WARNING: this object will keep a pointer to this <code>QDGraphics</code>, and unless
	 * you explicitly call <code>setDisposing(false)</code> then <code>disposeQTObject()</code>
	 * will be called when this object is finalized.  This is designed to help
	 * minimize the worrying you have to show over memory allocation, but if you
	 * intend to use this <code>QDGraphics</code> beyond the scope of
	 * this object, you should call <code>setDisposing(false)</code>.
	 */
	public QDGraphics getQDGraphics() {
		return g;
	}
	
	/** Subsequent calls to <code>getImage</code> will flip
	 * the image horizontally based on <code>b</code>.
	 * <P>This may be useful for cameras to flip an image as it
	 * is being read.
	 * @param b whether this image should be flipped horizontally.
	 */
	public void setFlipHorizontal(boolean b) {
		flipHorizontal = b;
	}
	
	/** @return whether this image will be flipped horizontally when
	 * it is transposed from QT-space to Java-space
	 */
	public boolean isFlipHorizontal() {
		return flipHorizontal;
	}
	
	/** Subsequent calls to <code>getImage</code> will flip
	 * the image vertically based on <code>b</code>.
	 * @param b whether this image should be flipped vertically.
	 */
	public void setFlipVertical(boolean b) {
		flipVertical = b;
	}
	
	/** @return whether this image will be flipped vertically when
	 * it is transposed from QT-space to Java-space
	 */
	public boolean isFlipVertical() {
		return flipVertical;
	}
	
	/** This converts the underlying <code>QDGraphics</code>
	 * into a java.awt-friendly <code>BufferedImage</code>.
	 * @param bi the destination image, or <code>null</code>.
	 * <P>This image must of type <code>BufferedImage.TYPE_INT_ARGB</code>.
	 * (Although it will not be hard for other programmers to support different
	 * image types, this is what the current version of this class requires.)
	 * <P>If non-null, then this image must be the dimensions of this image,
	 * because the QT image will be copied pixel-for-pixel directly into this
	 * image.
	 * @return the same image that was passed as the argument, if the argument
	 * was non-null.  If the argument was null, then this returns a new
	 * <code>BufferedImage</code>.  The image that is returned here will represent
	 * pixel for pixel what the underlying <code>QDGraphics</code> represents.
	 */
	public synchronized BufferedImage getImage(BufferedImage bi) {
		if(pixMap==null || g==null)
			throw new RuntimeException("This image is invalid: either finalize() has already been invoked or this object was not constructed correctly.");
		if(bi==null) {
			bi = new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
		} else if(bi.getType()!=BufferedImage.TYPE_INT_RGB) {
			throw new IllegalArgumentException("This method requires an image of type BufferedImage.TYPE_INT_RGB.");
		} else if(bi.getWidth()!=d.width || bi.getHeight()!=d.height) {
			throw new IllegalArgumentException("The BufferedImage provided was "+bi.getWidth()+"x"+bi.getHeight()+" pixels, but this image is "+d.width+"x"+d.height+" pixels.");
		}

		if(qtData!=null) {
			pixMap.getPixelData().copyToArray(0,qtData,0,qtData.length);
		} else {
			qtData = pixMap.getPixelData().getBytes();
		}

		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 0;
		int y, x, y2, javaOffset, qtOffset;
		boolean reportedOpaquePixel = false;
		
		switch(pixelFormat) {
			case QDConstants.k32ARGBPixelFormat:
			case QDConstants.k32ABGRPixelFormat:
			case QDConstants.k32BGRAPixelFormat:
			case QDConstants.k32RGBAPixelFormat:
			case QDConstants.k24BGRPixelFormat:
			case QDConstants.k24RGBPixelFormat:
				break;
			default:
				throw new RuntimeException("Unsupported pixel format ("+pixelFormat+")");
		}
		
		for(y = 0; y<d.height; y++) {
			for(x = 0; x<d.width; x++) {
				javaOffset = flipHorizontal ? (d.width-1-x) : x;
				qtOffset = y*rl+x*bpp;
				if(pixelFormat==QDConstants.k32ARGBPixelFormat) {
					alpha = (qtData[qtOffset+0] & (0xFF));
					red = (qtData[qtOffset+1] & (0xFF));
					green = (qtData[qtOffset+2] & (0xFF));
					blue = (qtData[qtOffset+3] & (0xFF));
				} else if(pixelFormat==QDConstants.k32ABGRPixelFormat) {
					alpha = (qtData[qtOffset+0] & (0xFF));
					blue = (qtData[qtOffset+1] & (0xFF));
					green = (qtData[qtOffset+2] & (0xFF));
					red = (qtData[qtOffset+3] & (0xFF));
				} else if(pixelFormat==QDConstants.k32BGRAPixelFormat) {
					blue = (qtData[qtOffset+0] & (0xFF));
					green = (qtData[qtOffset+1] & (0xFF));
					red = (qtData[qtOffset+2] & (0xFF));
					alpha = (qtData[qtOffset+3] & (0xFF));
				} else if(pixelFormat==QDConstants.k32RGBAPixelFormat) {
					red = (qtData[qtOffset+0] & (0xFF));
					green = (qtData[qtOffset+1] & (0xFF));
					blue = (qtData[qtOffset+2] & (0xFF));
					alpha = (qtData[qtOffset+3] & (0xFF));
				} else if(pixelFormat==QDConstants.k24BGRPixelFormat) {
					blue = (qtData[qtOffset+0] & (0xFF));
					green = (qtData[qtOffset+1] & (0xFF));
					red = (qtData[qtOffset+2] & (0xFF));
					alpha = 255;
				} else if(pixelFormat==QDConstants.k24RGBPixelFormat) {
					red = (qtData[qtOffset+0] & (0xFF));
					green = (qtData[qtOffset+1] & (0xFF));
					blue = (qtData[qtOffset+2] & (0xFF));
					alpha = 255;
				}
				//although it would make the code messier, if you have any
				//filters to the incoming RGB data this would be an excellent
				//place to apply them... I would recommend NOT wrapping
				//them in another method: that can add overhead to what should
				//be a very tight loop.
				newArray[javaOffset] = (red << 16)+(green << 8)+blue;
				
				if(alpha>0)
					reportedOpaquePixel = true; //more on this later
			}
			y2 = flipVertical ? d.height-y-1 : y;
			bi.getRaster().setDataElements(0,y2,d.width,1,newArray);
		}
		if(reportedOpaquePixel==false) {
			/* An amazing little bug on Windows machines: the alpha component of the every pixel could be 0%,
			* when in fact it was supposed to be 100% opaque.  So to catch this little problem, here I 
			* make the entire image opaque if I notice EVERYTHING so far has been transparent.  An awkward patch,
			* but unless I find out what's causing the problem, it's the best I can do.
			*/
			makeOpaque(bi,newArray);
		}
		return bi;
	}
	
	/** Takes an ARGB image and makes every pixel opaque
	 * 
	 * @param bi an image of type BufferedImage.TYPE_INT_ARGB
	 * @param d an array of length bi.getWidth()
	 */
	private static void makeOpaque(BufferedImage bi,int[] d) {
		int h = bi.getHeight();
		int w = bi.getWidth();
		for(int y = 0; y<h; y++) {
			bi.getRaster().getDataElements(0,y,w,1,d);
			for(int x = 0; x<w; x++) {
				d[x] = 0xff000000+(d[x] & 0xffffff);
			}
			bi.getRaster().setDataElements(0,y,w,1,d);
		}
	}
	

}
