/*
 * @(#)FSUtils.java
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
package com.bric.image.transition.swf;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.DataFormatException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.bric.geom.ShapeBounds;
import com.flagstone.transform.FSBounds;
import com.flagstone.transform.FSColor;
import com.flagstone.transform.FSColorTable;
import com.flagstone.transform.FSCoordTransform;
import com.flagstone.transform.FSMovieObject;
import com.flagstone.transform.FSShape;
import com.flagstone.transform.FSSolidFill;
import com.flagstone.transform.FSSolidLine;
import com.flagstone.transform.FSSoundStreamBlock;
import com.flagstone.transform.util.FSShapeConstructor;
import com.flagstone.transform.util.FSSoundConstructor;

/** A collection of static methods to bridge the gap from Java AWT objects
 * to Flagstone objects.
 */
public class FSUtils {

	/** @return a FSColor based on a java.awt.Color */
	public static FSColor createFSColor(Color c) {
		return new FSColor(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	}
	
	/** @return a FSCoordTransform based on an AffineTransform */
	public static FSCoordTransform createFSCoordTransform(AffineTransform t) {
		if(t==null)
			return new FSCoordTransform();
		return new FSCoordTransform(new float[][] { 
				{(float)t.getScaleX(), (float)t.getShearX(), (float)t.getTranslateX()},
				{(float)t.getShearY(), (float)t.getScaleY(), (float)t.getTranslateY()},
				{0, 0, 1}});
	}

	/** @return the bounds of a java Shape */
	public static FSBounds getBounds(Shape javaShape) {
		Rectangle r = ShapeBounds.getBounds(javaShape).getBounds();
		return new FSBounds(r.x,r.y,r.x+r.width,r.y+r.height);
	}
	
	/** @return an FSShape based on a java.awt.Shape
	 */
	public static FSShape createFSShape(Shape javaShape) {
		//disclaimer: There are some things about this method I do
		//not understand.  (Such as why we have to define a line/fill style
		//for this to work?)
		
		//So maybe this can get cleaned up?  I just know this
		//works 95% of the time now.
		
		FSShapeConstructor fsc = new FSShapeConstructor();
		fsc.COORDINATES_ARE_PIXELS = false;
		
		PathIterator i = javaShape.getPathIterator(null,1);
        if(i.getWindingRule()==PathIterator.WIND_NON_ZERO) {
            Area area = new Area(javaShape);
            i = area.getPathIterator(null,1);
        }
		float[] coords = new float[6];
		
	    fsc.add(new FSSolidLine(1, FSColorTable.black()));
	    fsc.add(new FSSolidFill(FSColorTable.black()));
	    boolean pathStarted = false;
	    float moveX = 0;
	    float moveY = 0;
		while(i.isDone()==false) {
			int k = i.currentSegment(coords);
			if(k==PathIterator.SEG_MOVETO) {
				if(pathStarted==false) {
					pathStarted = true;
					fsc.newPath();
				}
			    fsc.selectStyle(0, 0);
			    moveX = coords[0];
			    moveY = coords[1];
				fsc.move( (int)(coords[0]+.5f), (int)(coords[1]+.5f) );
			} else if(k==PathIterator.SEG_LINETO) {
				fsc.line( (int)(coords[0]+.5f), (int)(coords[1]+.5f) );
			} else if(k==PathIterator.SEG_QUADTO) {
				fsc.curve( (int)(coords[0]+.5f), (int)(coords[1]+.5f),
						(int)(coords[2]+.5f), (int)(coords[3]+.5f) );
			} else if(k==PathIterator.SEG_CUBICTO) {
				fsc.curve( (int)(coords[0]+.5f), (int)(coords[1]+.5f),
						(int)(coords[2]+.5f), (int)(coords[3]+.5f),
						(int)(coords[4]+.5f), (int)(coords[5]+.5f) );
			} else if(k==PathIterator.SEG_CLOSE) {
				fsc.line( (int)(moveX+.5f), (int)(moveY+.5f) );
				fsc.closePath();
			}
			i.next();
		}

		fsc.closePath();
		
		return fsc.shape();
	}
	
	/** This takes an audio file and returns an iterate that can ration out
	 * blocks of the sound file over several frames.
	 * @return an iterator that returns FSMovieObjects that can be added
	 * to your movie, one object per frame, to stream a sound.
	 * @throws IllegalArgumentException if an error occurred parsing the arguments.
	 */
	public static Iterator<FSMovieObject> getAudioBlockIterator(File file,int frameRate) throws IllegalArgumentException {
		try {
			return new FSBlockAudioIterator(file,frameRate);
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			IllegalArgumentException e2 = new IllegalArgumentException("The audio file \""+file.getName()+"\" could not be encoded.");
			e2.initCause(e);
			throw e2;
		}
	}
}

class FSBlockAudioIterator implements Iterator<FSMovieObject> {
	FSSoundConstructor soundGenerator;
	int samplesPerBlock;
	int numberOfBlocks;
	int currentBlock;
	
	public FSBlockAudioIterator(File file,int frameRate) throws IOException, DataFormatException, UnsupportedAudioFileException {
		String t = file.getName();
		if(t.indexOf('.')==-1) {
			throw new IllegalArgumentException("The file \""+file.getName()+"\" must have a suffix.");
		}
		t = t.substring(t.lastIndexOf('.')+1).toLowerCase();
		
		if (!(t.equals("wav") || t.equals("mp3"))) { //$NON-NLS-1$ //$NON-NLS-2$
			if(t.equals("aiff") || t.equals("aif")) {
				//java classes can handle this:
				AudioInputStream in = AudioSystem.getAudioInputStream(file);
				File newWAVFile = File.createTempFile("convertedAIFF","wav");
				AudioSystem.write(in, AudioFileFormat.Type.WAVE, newWAVFile);
				file = newWAVFile;
			} else {
				throw new IllegalArgumentException("The file \""+file.getName()+"\" is not a supported audio file.");
			}
		}
		soundGenerator = new FSSoundConstructor(file.getAbsolutePath());

		/* This is used to stream the sound:
		 */
		samplesPerBlock = soundGenerator.getSampleRate() / frameRate;
		numberOfBlocks = soundGenerator.getSamplesPerChannel()
				/ samplesPerBlock;
		currentBlock = 0;
	}

	public boolean hasNext() {
		return currentBlock < numberOfBlocks;
	}

	private boolean createdHeader = false;
	public FSMovieObject next() {
		if(createdHeader==false) {
			createdHeader = true;
			return soundGenerator.streamHeader(samplesPerBlock);
		}
		FSSoundStreamBlock returnValue = soundGenerator.streamBlock(currentBlock, samplesPerBlock);
		currentBlock++;
		return returnValue;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
