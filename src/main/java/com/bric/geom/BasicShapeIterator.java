/*
 * @(#)BasicShapeIterator.java
 *
 * $Date: 2014-04-14 08:05:51 +0200 (Mo, 14 Apr 2014) $
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
package com.bric.geom;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/** This is the <code>PathIterator</code> that
 * iterates over a BasicShape.
 * <P>
 */
class BasicShapeIterator implements PathIterator {
    BasicShapeSegment t;
    double m00, m10, m01, m11, m02, m12;
    boolean usingOriginal = true;
    
    BasicShapeIterator(BasicShape s,AffineTransform transform) {
        t = s.list;
        if(transform==null) {
            m00 = m11 = 1;
            m01 = m10 = m02 = m12 = 0;
        } else {
            double[] m = new double[6];
            transform.getMatrix(m);
            m00 = m[0];
            m10 = m[1];
            m01 = m[2];
            m11 = m[3];
            m02 = m[4];
            m12 = m[5];
        }
    }
    
    /** This clones our own copy of the shape data
     * if we haven't already done so.
     * <p>This method is <i>not</i> called by default,
     * because ideally we'd like to not clone all those arrays
     * as we iterate over the shape.  However if the
     * segments we're referencing need to change
     * (because the BasicShape is somehow being altered),
     * then this method will be called.
     */
    void releaseOriginal() {
    	if(usingOriginal==false)
    		return;
    	usingOriginal = false;
    	if(t==null)
    		return;
    	
    	t = (BasicShapeSegment)t.clone();
    }
    
    
    public int currentSegment(double[] coords) {
    	double x, y;

    	for(int a = 0; a<t.data.length; a+=2) {
    		x = t.data[a];
    		y = t.data[a+1];
            coords[a] = x*m00+y*m01+m02;
            coords[a+1] = x*m10+y*m11+m12;
    	}
    	
        return t.type;
    }
    
    public int currentSegment(float[] coords) {
    	double x, y;

    	for(int a = 0; a<t.data.length; a+=2) {
    		x = t.data[a];
    		y = t.data[a+1];
            coords[a] = (float)(x*m00+y*m01+m02);
            coords[a+1] = (float)(x*m10+y*m11+m12);
    	}
        return t.type;
    }
    
    public int getWindingRule() {
        return PathIterator.WIND_EVEN_ODD;
    }
    
    public boolean isDone() {
        if(t==null)
            return true;
        return false;
    }
    
    public void next() {
        if(t==null) {
            throw new IllegalStateException("No more segments to iterate over.");
        }
        t = t.next;
    }
}
