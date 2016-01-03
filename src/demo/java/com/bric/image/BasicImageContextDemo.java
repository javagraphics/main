/*
 * @(#)ImageContextDemo.java
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

/** This runs performance comparisons of different BasicImageContexts.
 * @see com.bric.image.BasicImageContext_v1
 * @see com.bric.image.BasicImageContext_v2
 * @see com.bric.image.BasicImageContext_v3
 * @see com.bric.image.BasicImageContext_v4
 * @see com.bric.image.BasicImageContext_v5
 * @see com.bric.image.BasicImageContext_v6
 * @see com.bric.image.BasicImageContext_v7
 * @see com.bric.image.BasicImageContext_v8
 * @see com.bric.image.BasicImageContext_v9
 * @see com.bric.image.BasicImageContext_v10
 * @see com.bric.image.BasicImageContext_v11
 * @see com.bric.image.BasicImageContext_v12
 * @see com.bric.image.BasicImageContext_v13
 * @see <a href="http://javagraphics.blogspot.com/2014/05/images-3d-transitions-and.html">Images: 3D Transitions and PerspectiveTransforms</a>
 */
public class BasicImageContextDemo extends AbstractImageContextDemo {
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
				f.getContentPane().add(new BasicImageContextDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	public BasicImageContextDemo() {
		super("BasicImage Optimizations:", new Object[] { "Description", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11", "v12", "v13" } );
	}
	
	@Override
	protected Object createTableValue(int col,Object interpolationHint,boolean useAlpha,String name) {
		try {
			Runtime.getRuntime().runFinalization();
			Runtime.getRuntime().gc();
			Thread.sleep(1000);
			
			BufferedImage src = useAlpha ? createTranslucentCopy(sampleImage, .6f) : sampleImage;
			int type = useAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
			BufferedImage dst = new BufferedImage(1000, 1000, type);
			
			String filename = "";
			long[] times = new long[5];
			for(int i = 0; i<times.length; i++) {
				clearImage(dst);
				times[i] = System.currentTimeMillis();
				ImageContext context = null;
				if(col==1) {
					context = new BasicImageContext_v1(dst);
				} else if(col==2) {
					context = new BasicImageContext_v2(dst);
				} else if(col==3) {
					context = new BasicImageContext_v3(dst);
				} else if(col==4) {
					context = new BasicImageContext_v4(dst);
				} else if(col==5) {
					context = new BasicImageContext_v5(dst);
				} else if(col==6) {
					context = new BasicImageContext_v6(dst);
				} else if(col==7) {
					context = new BasicImageContext_v7(dst);
				} else if(col==8) {
					context = new BasicImageContext_v8(dst);
				} else if(col==9) {
					context = new BasicImageContext_v9(dst);
				} else if(col==10) {
					context = new BasicImageContext_v10(dst);
				} else if(col==11) {
					context = new BasicImageContext_v11(dst);
				} else if(col==12) {
					context = new BasicImageContext_v12(dst);
				} else if(col==13) {
					context = new BasicImageContext_v13(dst);
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
				times[i] = System.currentTimeMillis() - times[i];
				
				filename = context.getClass().getName();
				filename = filename.substring(filename.lastIndexOf('.')+1);
				filename = name + " " + filename;
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
