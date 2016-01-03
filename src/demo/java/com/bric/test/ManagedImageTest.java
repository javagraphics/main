/*
 * @(#)ManagedImageTest.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

import com.bric.blog.Blurb;

/** This demonstrates the drastic performance problems you can
 * see if you unmanage your images.
 * <P>Note: recent JVMs do not seem to demonstrate this problem.
 * 
 */
@Blurb (
filename = "ManagedImageTest",
title = "Images: Managed Images",
releaseDate = "April 2007",
summary = "There used to be a way to completely ruin your graphics performance.\n"+
"<p>This problem can still occur on Mac if you're use the Quartz rendering engine, but "+
"more and more that is being phased out so this is less of a problem than it used to be.",
link = "http://javagraphics.blogspot.com/2007/04/managed-images.html",
sandboxDemo = false
)
public class ManagedImageTest {
    
    /** This program demonstrates the performance difference you see if you
     * un-manage BufferedImage's.
     * @param args the application's arguments. (This is unused.)
     */
    public static void main(String[] args) {
        BufferedImage bi = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
        //in this image we use getDataBuffer() to unmanage it:
        BufferedImage dest1 = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
        //in this image we use getSource() to unmanage it:
        BufferedImage dest2 = new BufferedImage(500,500,BufferedImage.TYPE_INT_ARGB);
        System.out.println("Regular Test Time (1): "+runTest(dest1,bi));
        //now I'll un-manage image 1:
        dest1.getRaster().getDataBuffer();
        System.out.println("Unmanaged Test Time (1): "+runTest(dest1,bi));
        
        System.out.println("Regular Test Time (2): "+runTest(dest2,bi));
        //now I'll un-manage image 2:
        ImageConsumer ic = new NullImageConsumer();
        dest2.getSource().addConsumer(ic);
        System.out.println("Unmanaged Test Time (2): "+runTest(dest2,bi));
        //note the damage is done: removing the consumer doesn't manage your image again:
        dest2.getSource().removeConsumer(ic);
        System.out.println("Unmanaged Test Time (2): "+runTest(dest2,bi));
        System.exit(0);
    }
    
    /** This is just a stub. */
    static class NullImageConsumer implements ImageConsumer {
        public void imageComplete(int status) {}
        public void setColorModel(ColorModel model) {}
        public void setDimensions(int width, int height) {}
        public void setHints(int hintflags) {}
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {}
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {}
        public void setProperties(Hashtable<?, ?> props) {}        
    }
    
    public static long runTest(BufferedImage dest,BufferedImage src) {
        long[] times = new long[20];
        Random r = new Random(0);
        for(int a = 0; a<times.length; a++) {
            long t = System.currentTimeMillis();
            Graphics2D g = dest.createGraphics();
            for(int b = 0; b<500; b++) {
                g.drawImage(src,
                        (int)(450*r.nextDouble()),
                        (int)(450*r.nextDouble()),
                        null);
            }
            g.dispose();
            t = System.currentTimeMillis()-t;
            times[a] = t;
        }
        Arrays.sort(times);
        //return the median time
        return times[times.length/2];
    }
}
