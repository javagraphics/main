/*
 * @(#)ImageContextDemo.java
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** This runs comparisons of different ImageContexts.
 * 
 * @see <a href="http://javagraphics.blogspot.com/2014/05/images-3d-transitions-and.html">Images: 3D Transitions and PerspectiveTransforms</a>
 */
public class ImageContextDemo extends AbstractImageContextDemo {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JFrame f = new JFrame();
				f.getContentPane().add(new ImageContextDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	public ImageContextDemo() {
		super("ImageContext Comparisons:", new Object[] { "Description", "Graphics2D", "BasicImageContext", "JFXImageContext", "ParallelImageContext", "SCImageContext" } );
	}
	

	@Override
	protected Object createTableValue(int col, Object interpolationHint,
			boolean useAlpha, String name) {

		try {
			Runtime.getRuntime().runFinalization();
			Runtime.getRuntime().gc();
			Thread.sleep(1000);
			BufferedImage src = useAlpha ? createTranslucentCopy(sampleImage, .6f) : sampleImage;
			int type = useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
			if(col==3) {
				//apparently JFX only wants to deal with this kind of image:
				type = BufferedImage.TYPE_INT_ARGB_PRE;
			}
			BufferedImage dst = new BufferedImage(1000, 1000, type);
			
			String filename = "";
			long[] times = new long[5];
			for(int i = 0; i<times.length; i++) {
				clearImage(dst);
				times[i] = System.currentTimeMillis();
				
				if(col==1) {
					//we're using a Graphics2D:
					Graphics2D g = dst.createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
					for(int imageIndex = 0; imageIndex<100; imageIndex++) {
						AffineTransform transform = getImageTransform(imageIndex);
						g.setTransform(transform);
						g.drawImage(src, 0, 0, null);
					}
					g.dispose();
					
					filename = "Graphics2D";
				} else {
					ImageContext context = null;
					if(col==2) {
						context = new BasicImageContext(dst);
					} else if(col==3) {
						context = new JFXImageContext(dst);
					} else if(col==4) {
						//context = new ParallelImageContext(dst);
						return "Unimplemented";
					} else if(col==5 && 
							RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.equals(interpolationHint)) {
						context = new SCImageContext(dst);
					} else {
						return "Unrecognized";
					}
					
					context.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
					for(int imageIndex = 0; imageIndex<100; imageIndex++) {
						AffineTransform transform = getImageTransform(imageIndex);
						Point2D topLeft = new Point2D.Double(0,0);
						Point2D topRight = new Point2D.Double(src.getWidth(),0);
						Point2D bottomLeft = new Point2D.Double(0,src.getHeight());
						Point2D bottomRight = new Point2D.Double(src.getWidth(),src.getHeight());
						transform.transform(topLeft, topLeft);
						transform.transform(topRight, topRight);
						transform.transform(bottomLeft, bottomLeft);
						transform.transform(bottomRight, bottomRight);
						context.drawImage(src, topLeft, topRight, bottomRight, bottomLeft);
					}
					context.dispose();
					filename = context.getClass().getName();
					filename = filename.substring(filename.lastIndexOf('.')+1);
				}
				times[i] = System.currentTimeMillis() - times[i];
				
				filename = name +" "+ filename;
			}
			ImageIO.write(dst, "png", new File(filename+".png"));
			Arrays.sort(times);
			return times[times.length/2];
		} catch(Throwable t) {
			t.printStackTrace();
			return "Error";
		}
		
	}
}
