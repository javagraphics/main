/*
 * @(#)QTAnimationReader.java
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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

import quicktime.QTException;
import quicktime.QTSession;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;
import quicktime.std.movies.TimeInfo;
import quicktime.std.movies.media.FlashMedia;
import quicktime.std.movies.media.Media;
import quicktime.std.movies.media.VideoMedia;

/** This iterates over a QT animation returning visual data for each
 * frame.  This class is actually extracted from my own codebase
 * where I have an abstract AnimationReader interface, so this is designed
 * to work similar to an Iterator.  This class could be fleshed out so it
 * allows random access to frames, but for now it's designed primarily to iterate
 * over the data once.
 * <P>This is no way addresses the audio data in a QT file.
 * <P>The recommended usage for this class is as follows:
 * <BR>
 * <BR><code>QTAnimationReader r = new QTAnimationReader(file);</code>
 * <BR><code>for(int frame = 0; frame &lt; r.getFrameCount(); frame++) {</code>
 * <BR><code> &nbsp; BufferedImage bi = r.getNextFrame(false);</code>
 * <BR><code> &nbsp; processFrame(bi, r.getFrameLength());</code>
 * <BR><code>}</code>
 * <P>There is a <code>main()</code> method included that provides
 * a very minimal implementation of this class.
 * <P>(Also if "Enabled Flash" is active in your QT preferences, this will
 * work for some SWF files.)
 * 
 * @deprecated
 */
public class QTAnimationReader {
	int ctr = 0;
	boolean disposeWhenFinished;
	int durationInMillis;
	QTJImage i;
	Movie movie;
	OpenMovieFile of;
	TimeRecord r;
	/** This is the image we paint everything to */
	BufferedImage recycledImage;
	int[] times;
	
	int w, h;
	
	/** Initializes a QTAnimationReader from a QuickTime file.
	 * This will thrown an exception if the file is not a 
	 * QuickTime supported file; the behavior of this class is
	 * undefined if the file passed here is a still image.
	 * @param file a file to read
	 * @throws QTException
	 */
	public QTAnimationReader(File file) throws QTException {
		QTSession.open();
		QTFile qtf = new QTFile(file);
		of = OpenMovieFile.asRead(qtf);
		Movie m = Movie.fromFile(of);
		init(m,true);
	}
	
	/** Initializes a QTAnimationReader.
	 * 
	 * @param movie this movie will have its time adjusted, so by passing it
	 * here you should be surrending control of it to this class for a while.
	 * @param disposeWhenFinished if this is <code>true</code>, then the
	 * movie is disposed when this object finishes iterating over the file.
	 * @throws QTException
	 */
	public QTAnimationReader(Movie movie,boolean disposeWhenFinished) throws QTException {
		init(movie,disposeWhenFinished);
	}
	
	/** Releases resources */
	@Override
	protected void finalize() throws Throwable {
		releaseQTResources();
		recycledImage = null;
		super.finalize();
	}
	
	/** @return the duration in milliseconds of this animation */
	public int getDuration() {
		return durationInMillis;
	}
	
	/** @return the number of frames this reader will return. */
	public int getFrameCount() {
		return times.length;
	}
	
	/** @return the duration of the frame last
	 * returned by getNextFrame() in milliseconds.
	 **/
	public int getFrameLength() {
		int t = times[ctr]-times[ctr-1];
		return t*1000/r.getScale();
	}

	/** @return the height in pixels of this animation */
	public int getHeight() {
		return h;
	}

	/** This iterates over each visual frame of this animation. 
	 * This will return <code>null</code> when there are no more
	 * frames to read.
	 * @param cloneImage if this is <code>true</code>, then a new cloned
	 * image will be returned each time.  If this is <code>false</code>,
	 * then the same image is always returned.  It is recommended that this
	 * be <code>false</code>, since that will really cut down on overhead.
	 * @return the next frame, or <code>null</code> if there are no new frames.
	 * @throws IOException
	 */
	public BufferedImage getNextFrame(boolean cloneImage) throws IOException {
		if(ctr==times.length) {
			releaseQTResources();
			QTSession.close();
			return null;
		}
		
		try {
			r.setValue(times[ctr]);
			movie.setTime(r);
			movie.task(999999);
			
			ctr++;
			
			if(!cloneImage) {
				recycledImage = i.getImage(recycledImage);
				return recycledImage;
			}
			BufferedImage bi = i.getImage(null);
			return bi;
		} catch(QTException e) {
			e.printStackTrace();
			throw new RuntimeException("A QuickTime related error has occurred.");
		}
	}

	/** @return the width of this animation */
	public int getWidth() {
		return w;
	}

	private void init(Movie m,boolean dispose) throws QTException {
		movie = m;
		disposeWhenFinished = dispose;
		
		movie.setActive(true);
		w = movie.getBounds().getWidth();
		h = movie.getBounds().getHeight();
		i = new QTJImage(new Dimension(w,h));
		movie.setGWorld(i.getQDGraphics(),null);
		
		TreeSet<Number> list = new TreeSet<Number>();
		Media[] media = new Media[movie.getTrackCount()]; //look at every media
		for(int a = 0; a<media.length; a++) {
			media[a] = movie.getIndTrack(a+1).getMedia();
			if(media[a] instanceof VideoMedia || media[a] instanceof FlashMedia) {
				boolean isMPEG = (media[a].getHandlerDescription().subType!=StdQTConstants.MPEGMediaType);
				if (isMPEG) {
					//MPEGs don't give us as much info as other media
					listAllTimes(list,24);
				} else {
					listInterestingTimes(media[a],list);
				}
	        }
		}
		r = new TimeRecord(movie.getTimeScale(),0);
		durationInMillis = (1000*movie.getDuration()/movie.getTimeScale());
		times = new int[list.size()];
		ctr = 0;
		Iterator<Number> z = list.iterator();
		while(z.hasNext()) {
			times[ctr] = (z.next()).intValue();
			ctr++;
		}
		ctr = 0;
	}

	/** List all times based on a given frame rate.
	 * 
	 * TODO: A similar approach was used in the Tech4Learning codebase,
	 * and it was dramatically improved when the QTJImage was able to
	 * report back that a BufferedImage had not changed as a result of being
	 * updated.  When this was the case, the iterator skipped those frames.
	 * So the frame count was continually reducing in some cases, and the duration
	 * of a frame increased, but the overall number of frames was reduced.
	 * 
	 * I'm not implementing this here simply because QTJ is on its way out, so
	 * I don't want to invest a lot of time on this kind of code.
	 *  - Januay 2009
	 *  
	 */
	protected void listAllTimes(TreeSet<Number> dest,int frameRate) throws QTException {
		int t = 0;
		int movieDuration = movie.getDuration();
		while(t<movieDuration) {
			dest.add(new Integer(t));
			t+=movie.getTimeScale()/frameRate;
		}
		dest.add(new Integer(movieDuration));
	}
	
	/** List all the interesting times in a particular media */
	protected void listInterestingTimes(Media media,TreeSet<Number> dest) throws QTException {
		int t = 0;
		int movieDuration = movie.getDuration();
		int timeScale = movie.getTimeScale();
		TimeInfo ti;
		
		//note t (our time) is relative to the MOVIE's timeScale and duration, we have
		//to convert it to each specific media's timescale to make sense.
		while(t<movieDuration) {
			dest.add(new Integer(t)); //note this frame is of interest
						
			int lastTime = (int)(.5F+t*media.getTimeScale()/timeScale);
			ti = media.getNextInterestingTime(StdQTConstants.nextTimeMediaSample,lastTime,.0001F);
			if(ti.time>0) {
				t = Math.min(movieDuration, (int)(((float)ti.time)*timeScale/media.getTimeScale()) );
			}
		}
		dest.add(new Integer(movieDuration));
	}

	/** Releases QT resources */
	protected void releaseQTResources() {
		try {
			if(movie!=null) {
				movie.setActive(false);
				if( disposeWhenFinished ) {
					movie.disposeQTObject();
				}
				movie = null;
			}
		} catch(QTException e) {
			e.printStackTrace();
		}
		if(r!=null) {
			try {
				r.disposeQTObject();
			} catch(QTException e) {
				e.printStackTrace();
			}
			 r = null;
		}
		if(of!=null) {
			try {
				of.close();
				of.disposeQTObject();
			} catch(QTException e) {
				e.printStackTrace();
			}
			of = null;
		}
	}

}
